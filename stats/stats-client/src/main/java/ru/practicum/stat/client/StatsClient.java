package ru.practicum.stat.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stat.dto.ParamDto;
import ru.practicum.stat.dto.EndpointHitDto;
import ru.practicum.stat.dto.ViewStatsDto;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Component
@Slf4j
public class StatsClient {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate template;
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statsServiceId;
    private final String appName;

    public StatsClient(RestTemplate template,
                       DiscoveryClient discoveryClient,
                       @Value("${stats-server.id:stats-server}") String statsServiceId,
                       @Value("${app.name:ewm-main-service}") String appName) {
        this.template = template;
        this.discoveryClient = discoveryClient;
        this.statsServiceId = statsServiceId;
        this.appName = appName;
        this.retryTemplate = initRetryTemplate();
    }

    private RetryTemplate initRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        template.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    private ServiceInstance getInstance() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(statsServiceId);
            if (instances.isEmpty()) {
                throw new IllegalStateException("Список инстансов пуст для сервиса: " + statsServiceId);
            }
            return instances.getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public void hit(EndpointHitDto endpointHit) {
        endpointHit.setApp(appName);
        try {
            URI uri = makeUri("/hit");
            HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHit);
            template.exchange(uri, POST, requestEntity, Object.class);
        } catch (RestClientException e) {
            log.warn("Не удалось сохранить хит: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> get(ParamDto paramDto) {
        URI baseUri = makeUri("/stats");

        URI uri = UriComponentsBuilder
                .fromUri(baseUri)
                .queryParam("start", paramDto.getStart().format(FORMATTER))
                .queryParam("end", paramDto.getEnd().format(FORMATTER))
                .queryParam("uris", paramDto.getUris())
                .queryParam("unique", paramDto.getUnique())
                .build()
                .encode()
                .toUri();

        try {
            ResponseEntity<List<ViewStatsDto>> response = template.exchange(
                    uri,
                    GET,
                    null,
                    new ParameterizedTypeReference<List<ViewStatsDto>>() {}
            );

            log.info("=== STATS CLIENT RESPONSE: status={}, body={} ===",
                    response.getStatusCode(), response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            log.warn("Ошибка при получении статистики: {}.", e.getMessage());
            return List.of(ViewStatsDto.builder().hits(-1L).build());
        }
    }
}
