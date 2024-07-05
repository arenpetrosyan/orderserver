package com.aren.orderserver.web.security.expression;

import com.aren.orderserver.enums.UserRole;
import com.aren.orderserver.services.OrderService;
import com.aren.orderserver.web.security.JwtEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("customSecurityExpression")
@RequiredArgsConstructor
public class CustomSecurityExpression {

    private final OrderService orderService;

    /**
     * Checks if the current user is allowed to post an order.
     *
     * @return true if the user has the role of a poster, false otherwise
     */
    public boolean canPostOrder() {
        return hasRole(SecurityContextHolder.getContext().getAuthentication(), UserRole.POSTER.name());
    }

    /**
     * Checks if the current user is allowed to process an order.
     *
     * @return true if the user has the role of a processor, false otherwise
     */
    public boolean canProcessOrder() {
        return hasRole(SecurityContextHolder.getContext().getAuthentication(), UserRole.PROCESSOR.name());
    }

    /**
     * Checks if the current user is allowed to get the details of a specific order.
     *
     * @param orderId The ID of the order to check permissions for
     * @return true if the user has the role of a processor or is the owner of the order, false otherwise
     */
    public boolean canGetOrder(Integer orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtEntity user = (JwtEntity) authentication.getPrincipal();
        Integer userId = user.getId();
        return hasRole(SecurityContextHolder.getContext().getAuthentication(), UserRole.PROCESSOR.name()) || isOrderOwner(userId, orderId);
    }

    /**
     * Checks if the user with the specified ID is the owner of the order with the specified ID.
     *
     * @param userId The ID of the user to check ownership
     * @param orderId The ID of the order to check ownership
     * @return true if the user is the owner of the order, false otherwise
     */
    private boolean isOrderOwner(Integer userId, Integer orderId) {
        return orderService.isOrderOwner(userId, orderId);
    }

    /**
     * Checks if the user has the specified role.
     *
     * @param authentication The authentication object of the user
     * @param role The role to check for
     * @return true if the user has the specified role, false otherwise
     */
    private boolean hasRole(Authentication authentication, String role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        return authentication.getAuthorities().contains(authority);
    }
}
