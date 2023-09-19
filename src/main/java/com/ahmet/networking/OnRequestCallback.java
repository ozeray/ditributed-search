package com.ahmet.networking;

public interface OnRequestCallback {

    // Request will be forwarded to coordinator/worker server
    byte[] handleRequest(byte[] requestPayload);

    // Will point to address of coordinator/worker server in the cluster
    String getEndpoint();
}
