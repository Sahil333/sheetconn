package com.sheetconn.connector.service;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.sheetconn.connector.controllers.dto.SheetProvider;
import com.sheetconn.connector.sheet.google.TypeConverterContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SpreadsheetWriterService {

    private final ConnectorRegistryService connectorRegistry;
    private final HttpTransport httpTransport;

    public SpreadsheetWriterService(ConnectorRegistryService connectorRegistry, HttpTransport httpTransport) {
        this.connectorRegistry = connectorRegistry;
        this.httpTransport = httpTransport;
    }

    // Can write to only google sheet provider for now
    public void write(String userId,
                      SheetProvider provider,
                      String workbookId,
                      String sheetId,
                      String range,
                      List<List<Object>> data) throws IOException {
        OAuth2AuthorizedClient authClient =
                connectorRegistry.fetchPrimarySheetAuthorizedClient(provider, userId);

        AccessToken accessToken = AccessToken.newBuilder()
                .setTokenValue(authClient.getAccessToken().getTokenValue())
                .setExpirationTime(Date.from(authClient.getAccessToken().getExpiresAt()))
                .setScopes(authClient.getAccessToken().getScopes().stream().toList())
                .build();

        GoogleCredentials credentials = GoogleCredentials.create(accessToken);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
                credentials);

        // Create the sheets API client
        Sheets service = new Sheets.Builder(httpTransport,
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Sheets samples")
                .build();

        UpdateValuesResponse result = null;

        convert(data);

        try {
            // Updates the values in the specified range.
            ValueRange body = new ValueRange()
                    .setValues(data);
            result = service.spreadsheets().values().update(workbookId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            log.info("{} cells updated.", result.getUpdatedCells());
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                log.error("Spreadsheet not found with id {}.\n", workbookId);
            } else {
                throw e;
            }
        }

        log.info("Updated result : {}", result);
    }

    private void convert(List<List<Object>> data) {
        TypeConverterContainer converter = new TypeConverterContainer();
        for (List<Object> datum : data) {
            datum.replaceAll(converter::convert);
        }
    }
}
