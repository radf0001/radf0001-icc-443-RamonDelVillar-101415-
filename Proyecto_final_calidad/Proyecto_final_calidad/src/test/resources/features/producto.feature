Feature: Gestión de productos

  Scenario: Crear un producto exitosamente
    Given el usuario está autenticado como administrador
    When el usuario navega a la vista de productos
    And completa el formulario con:
      | nombre      | Producto QA |
      | descripción | Prueba QA   |
      | categoría   | Otros       |
      | precio      | 10.50       |
      | cantidad    | 5           |
      | stockMinimo | 2           |
    And pulsa el botón Guardar
    Then ve la notificación "Producto guardado exitosamente"
