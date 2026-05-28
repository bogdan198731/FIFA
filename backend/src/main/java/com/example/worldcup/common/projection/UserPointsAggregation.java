package com.example.worldcup.common.projection;

/**
 * Spring Data projection used by the repositories that need to roll up
 * points-per-user without hydrating full entity rows.
 */
public interface UserPointsAggregation {

    Long getUserId();

    Long getTotal();
}
