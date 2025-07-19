package com.itu.socialcom.demo.socialmedia.service;

import com.itu.socialcom.demo.socialmedia.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Service for handling retry logic with exponential backoff for OAuth operations.
 * Implements intelligent retry strategies for different types of failures.
 */
@Service
public class RetryService {
    
    private static final Logger logger = LoggerFactory.getLogger(RetryService.class);
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long BASE_DELAY_MS = 1000; // 1 second
    private static final long MAX_DELAY_MS = 30000; // 30 seconds
    private static final double JITTER_FACTOR = 0.1; // 10% jitter
    
    /**
     * Execute an operation with retry logic and exponential backoff.
     * 
     * @param operation The operation to execute
     * @param operationName Name of the operation for logging
     * @param platform The platform being accessed
     * @return The result of the operation
     * @throws Exception If all retry attempts fail
     */
    public <T> T executeWithRetry(Supplier<T> operation, String operationName, String platform) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                logger.debug("Executing {} for platform {} (attempt {}/{})", 
                           operationName, platform, attempt, MAX_RETRY_ATTEMPTS);
                
                return operation.get();
                
            } catch (RateLimitExceededException e) {
                logger.warn("Rate limit exceeded for {} on platform {} (attempt {}/{}): {}", 
                          operationName, platform, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw e; // Don't retry rate limit errors on final attempt
                }
                
                // Use the retry-after value from the exception
                long delayMs = e.getRetryAfterSeconds() != null ? 
                              e.getRetryAfterSeconds() * 1000 : 
                              calculateExponentialDelay(attempt);
                
                waitWithJitter(delayMs);
                lastException = e;
                
            } catch (HttpServerErrorException e) {
                logger.warn("Server error during {} for platform {} (attempt {}/{}): {}", 
                          operationName, platform, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                
                waitWithJitter(calculateExponentialDelay(attempt));
                lastException = e;
                
            } catch (ResourceAccessException e) {
                logger.warn("Network error during {} for platform {} (attempt {}/{}): {}", 
                          operationName, platform, attempt, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                
                waitWithJitter(calculateExponentialDelay(attempt));
                lastException = e;
                
            } catch (Exception e) {
                // Don't retry for other types of exceptions (client errors, auth failures, etc.)
                logger.error("Non-retryable error during {} for platform {}: {}", 
                           operationName, platform, e.getMessage());
                throw e;
            }
        }
        
        // This should never be reached, but just in case
        throw lastException != null ? lastException : 
              new RuntimeException("All retry attempts failed for " + operationName);
    }
    
    /**
     * Execute an operation with retry logic for void operations.
     */
    public void executeWithRetry(Runnable operation, String operationName, String platform) throws Exception {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, operationName, platform);
    }
    
    /**
     * Calculate exponential backoff delay with maximum cap.
     */
    private long calculateExponentialDelay(int attempt) {
        long delay = BASE_DELAY_MS * (1L << (attempt - 1)); // 2^(attempt-1)
        return Math.min(delay, MAX_DELAY_MS);
    }
    
    /**
     * Wait for the specified delay with added jitter to prevent thundering herd.
     */
    private void waitWithJitter(long baseDelayMs) {
        double jitter = 1.0 + (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * JITTER_FACTOR;
        long delayWithJitter = (long) (baseDelayMs * jitter);
        
        logger.debug("Waiting {} ms before retry (base: {} ms)", delayWithJitter, baseDelayMs);
        
        try {
            Thread.sleep(delayWithJitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry wait interrupted", e);
        }
    }
    
    /**
     * Check if an exception is retryable.
     */
    public boolean isRetryableException(Exception e) {
        return e instanceof RateLimitExceededException ||
               e instanceof HttpServerErrorException ||
               e instanceof ResourceAccessException;
    }
    
    /**
     * Get the recommended delay for a specific exception type.
     */
    public long getRecommendedDelay(Exception e, int attempt) {
        if (e instanceof RateLimitExceededException) {
            RateLimitExceededException rateLimitEx = (RateLimitExceededException) e;
            return rateLimitEx.getRetryAfterSeconds() != null ? 
                   rateLimitEx.getRetryAfterSeconds() * 1000 : 
                   calculateExponentialDelay(attempt);
        }
        
        return calculateExponentialDelay(attempt);
    }
}