package org.example.proyecto_final_calidad.model;

/**
 * Enum representing user roles in the system.
 * Each role has specific permissions and access levels.
 */
public enum Role {
    /**
     * Administrator role with full access to all system functionalities.
     * Permissions:
     * - Product Management: Add, edit, delete, and view products
     * - User Management: Create and manage users
     */
    ADMINISTRATOR,

    /**
     * Manager role with access to product management but limited user management.
     * Permissions:
     * - Product Management: Add, edit, and view products
     * - Limited User Management: View users
     */
    EMPLOYEE,

    /**
     * User role with basic access to view products.
     * Permissions:
     * - Product Management: View products only
     */
    CUSTOMERGUEST
}
