Feature: Inicio de sesión

  Scenario: Usuario inicia sesión correctamente
    Given el usuario está en la página de login
    When ingresa usuario "admin" y contraseña "admin123"
    And hace clic en "btn-login"
    Then debería ver el dashboard
