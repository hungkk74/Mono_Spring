package com.monowear.config;


import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

/**
 * Manual CORS filter to ensure cross-origin requests from FE (localhost:3000) work reliably.
 * Handles both preflight OPTIONS requests and actual response headers.
 */
@Provider
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Handle preflight OPTIONS request immediately
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(
                    Response.ok()
                            .header("Access-Control-Allow-Origin", "*")
                            .header("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS")
                            .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
                            .header("Access-Control-Max-Age", "86400")
                            .build()
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type, Authorization");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "86400");
    }
}
