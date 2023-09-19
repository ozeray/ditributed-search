package com.ahmet.networking;

public interface OnRequestCallback {

    // Request will be forwarded to coordinator server
    byte[] handleRequest(byte[] requestPayload);

    // Will point to address of coordinator server in the cluster
    String getEndpoint();
}
