package com.sheetconn.connector.oauth.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Locator;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLConnection;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.net.URL;
import java.util.Base64;


@Slf4j
class GoogleSigningKeyLocator implements Locator<Key> {

    private static Cache<String, Key> googleSigningKeyCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(Duration.ofDays(1))
                    .maximumSize(100)
                    .build();

    private final ObjectMapper mapper;

    GoogleSigningKeyLocator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Key locate(Header header) {
        String kid = (String) header.get("kid");
        Key key = googleSigningKeyCache.getIfPresent(kid);

        if(key != null) return key;

        ArrayNode keys = (ArrayNode) fetchGooglePublicKeys();

        if(keys == null) {
            throw new RuntimeException("Failed to fetch google public keys");
        }

        for(JsonNode newKey : keys) {
            try {
                byte[] modulusBytes = Base64.getUrlDecoder().decode(newKey.get("n").asText());
                byte[] exponentBytes = Base64.getUrlDecoder().decode(newKey.get("e").asText());

                // Creating RSAPublicKeySpec
                BigInteger modulus = new BigInteger(1, modulusBytes);
                BigInteger exponent = new BigInteger(1, exponentBytes);
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);

                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey keyObject = kf.generatePublic(spec);
                googleSigningKeyCache.put(newKey.get("kid").asText(), keyObject);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        }

        return googleSigningKeyCache.getIfPresent(kid);
    }

    private JsonNode fetchGooglePublicKeys() {
        try {
            URL url = new URL("https://www.googleapis.com/oauth2/v3/certs");
            URLConnection connection = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return mapper.readTree(response.toString()).get("keys");
            }
        } catch (IOException ex) {
            log.error("Failed to fetch google signing keys", ex);
            return null;
        }
    }
}
