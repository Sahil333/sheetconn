package com.sheetconn.connector.service;

public class ConnectorConfigConstants {

    public static class Postgres {
        public static final String HOST = "host";
        public static final String PORT = "port";

        public static final String PASSWORD = "password";

        public static final String USER = "user";

        public static final String DATABASE = "database";
    }

    public static class OAuth {
        public static final String CLIENT_REGISTRATION_ID = "clientRegistrationId";
        public static final String ACCESS_TOKEN = "access_token";

        public static final String ACCESS_TOKEN_EXPIRES_AT = "access_token_expires_at";

        public static final String REFRESH_TOKEN = "refresh_token";

        public static final String REFRESH_TOKEN_EXPIRES_AT = "refresh_token_expires_at";

        public static final String SUBJECT = "sub";

        public static final String EMAIL = "email";

        public static final String NAME = "name";

        public static final String PICTURE = "picture";

        public static final String PHONE_NUMBER = "phone_number";
    }
}
