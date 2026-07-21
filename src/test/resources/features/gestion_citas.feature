Feature: Gestion de citas del taller mecanico

  Scenario: Se registra un mantenimiento ligero exito con otro mecanico
    Given un mecanico con especialidad MANTENIMIENTO_LIGERO disponible
    When se agenda un MANTENIMIENTO_LIGERO para la placa "ANA-525" el 15 de setiembre de 2026 a las 09:00
    Then la cita queda en estado PROGRAMADA
    And se notifica el agendamiento

  Scenario: Intentar agendar con el mecanico ocupado iniciando a las 11:00
    Given un mecanico que ya tiene una cita programada de 10:00 a 12:00 el 15 de setiembre de 2026
    When se intenta agendar una nueva cita con ese mecanico a las 11:00
    Then se rechaza el agendamiento por horario ocupado

  Scenario: Intentar agendar con el mecanico ocupado iniciando a las 12:00
    Given un mecanico que ya tiene una cita programada de 10:00 a 12:00 el 15 de setiembre de 2026
    When se intenta agendar una nueva cita con ese mecanico a las 12:00
    Then la cita se agenda correctamente porque no hay superposicion
