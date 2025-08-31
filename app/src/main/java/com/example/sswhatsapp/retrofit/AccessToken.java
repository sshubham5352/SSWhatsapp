package com.example.sswhatsapp.retrofit;

import android.util.Log;

import com.example.sswhatsapp.utils.Constants;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    private static final String firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"ss-whatsapp-84666\",\n" +
                    "  \"private_key_id\": \"11810a05758764e814dd07b3ff3fecb6cfa723ab\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCnsk+lKRrWCCag\\nWWMmAHrn0rQC7WELK9QbDZHoTt6WdUgfsUg7jyZQkGyqiQXXe73zET2wMgfKg3Ak\\nc8rn5duD1ORXXwx8EtW1dS8ML9IkfMLDhE5LzmsAwN9xEd3YoVyK031czoUsg7Eq\\nv/6H6CnknULOVuEGJUdJ7b0QmIATZDnVdAm08Wq7+Hjr1hIugcsNBjhn02nbD0Qi\\nvmlctaqej9dZcRZrSnaN208Hwu8K/b7rLfx6Xcy69b99bLZmrGRi/rz9mcS51hn5\\nItvIXGc/ITGE2h5s8ribSec9vX8T/YGpzg9J6tYERzyMLi3Ma4VYZANPovW7aUbf\\nLbHoP6T7AgMBAAECggEAAKZ+N5TtZTdv4D791Z0P3PaiEGws7FcAV2svOILsdbyW\\nXy/CqMnOgLvRUryoTfnNYUlyVKIaX0yGMqX+UKBBay/4A2dmIX8N3sp0YxbhDkIr\\nm92B/jpFNkyz2848GYXQBDvQvdDmjvx4be2DsQbopASAyKhpRABWApvjl/0MwMZ+\\nSFq5ASNvCKQw3RbmVWXK0XEGpMomcCjUgI/a77AqnpLyx0Jvv1ZktoIC4w/xHKSg\\nMkVYSJf2DUKd6u+FTDhjWaoxrBvd5r3oS5LP55mnko2wsrMca8ke7s6mu0wVcRVJ\\n8JWAZTqH2od/98ExrHER0M2P/3rp3zuLbXaLr2VDoQKBgQC5qO7XcgN3Sbp6i18V\\nuyqWXsiSJH3upakO9UK30XE2tbkzWkA/T+gLjmuLFYEV1/DexzewvgWgPv2md2wU\\nzTM3jyCGmw79o4Bj7W7Va9BrNKBIRQvRHfHM8+uNZe0ar3p52NQ8I7cR89TzxN9U\\nkSJgXoaujZ4JWY6em/xkOs5xWwKBgQDnOx6aT+rsfO1pXNeWlyNI7NhWtgcxXsK1\\nXs/3KnivmLJcnG9yx/rKyFFRuK7Jq4I1sJg8BJ4HVISXtiivSjhOhBXw6vrnYGPG\\nWSaIjFRp9BSM3UqSPSrV5z3pwi1Rl0/Os310aD/FAtQnnrBI5UKBmaFvPP0DvJZX\\nV4gcNyVM4QKBgC2Fm1JeFTV5cWy8oYrNgoquINWcdvg9zC7rufkiNHUqp4dM2LXt\\nXMMAn6PRmP4HHXsyM5h1byG0cDMR0wFisu0rfdkjSdCs9z0JKYTQ06CFncs6isQy\\naGhNYnMgC7vgTNJ1dMxuIWQYeh1vJycwEtK4OqOv+5DlUY/YRVE6DgZDAoGBAJR4\\n6IiLIjxhZwwNWjzOpRBxS+Q5k1rHDtzomT6Knd2afYRryj/4WneUsmp33HuPzdbt\\nooJ/MH1ibpT/mRYc4RTBSVldeGfTA53bppttB9LUr9FXUkeeUP/yMFYFoZXPQ6Zk\\np/xz5+Z3j4Q1J9pWoyKWZAsKLSgWEq7YfIQpgKDhAoGACxD8PJFPMBnubGa6KPtp\\n8qVKMpGRtPizD9X0+omuI+rottCDLqiIIbcqMu9fPaapZdcRqpgRGHH+phTno2Is\\nYtas+Juys7vEff3TYwL4oWLTNeDagLZZirKVIJS5KEpUuQrnAKBEPjW+nBnMmAvG\\nr4jd9M1+w2gZmR8NemfEHy4=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-knq6d@ss-whatsapp-84666.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"100113528285983397017\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-knq6d%40ss-whatsapp-84666.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList(firebaseMessagingScope));
            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();

        } catch (Exception e) {
            Log.d(Constants.APP_TAG, "getAccessToken: " + e.getMessage());
            return "";
        }
    }
}
