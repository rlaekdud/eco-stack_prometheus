package openstack.eco_stack.controller;

import lombok.extern.slf4j.Slf4j;
import openstack.eco_stack.domain.Token;
import openstack.eco_stack.service.CpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Slf4j
public class PrometheusDataFetcher {

    private final RestTemplate restTemplate;
    private final CpuService cpuService;

    @Autowired
    public PrometheusDataFetcher(RestTemplateBuilder restTemplateBuilder, CpuService cpuService) {
        this.restTemplate = restTemplateBuilder.build();
        this.cpuService = cpuService;
    }

    @GetMapping("/prometheus-metrics")
    public ResponseEntity<String> getPrometheusMetrics() throws URISyntaxException {
        String metricData = fetchMetricsData();
        String AUTH_TOKEN = new Token(new RestTemplateBuilder()).fetchToken();

        Pattern pattern = Pattern.compile("instanceName=\"([^\"]+)\".*projectId=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(metricData);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String instanceName = matcher.group(1);
            String projectId = matcher.group(2);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", AUTH_TOKEN);

            HttpEntity<?> entity = new HttpEntity<>(headers);

            String projectURL = "http://133.186.215.103/identity/v3/projects/" + projectId;

            ResponseEntity<String> project = restTemplate.exchange(projectURL, HttpMethod.GET, entity, String.class);

            String projectData = project.getBody();

            result.append("Instance Name: ").append(instanceName).append("\n");
            result.append("Project Id: ").append(projectId).append("\n");
            result.append("Project Info: ").append(projectData).append("\n");
            Map<String, Double> cpu_사용시간 = cpuService.cpu_사용시간(metricData);
            Map<String, Double> cpu_코어수 = cpuService.cpu_코어수(metricData);

            log.info("Number of Cores:");
            for (Map.Entry<String, Double> mp : cpu_코어수.entrySet()) {
                log.info("Project ID: " + mp.getKey() + ", CPU Metric: " + mp.getValue());
            }

            log.info("Usage Time:");
            for (Map.Entry<String, Double> entry : cpu_사용시간.entrySet()) {
                log.info("Project ID: " + entry.getKey() + ", Virtual CPU Metric: " + entry.getValue());
            }
        }
        return ResponseEntity.ok(result.toString());
    }

    private String fetchMetricsData() {
        String prometheusMetricsURL = "http://133.186.215.103:9000/metrics";
        ResponseEntity<String> response = restTemplate.getForEntity(prometheusMetricsURL, String.class);
        return response.getBody();
    }

}
