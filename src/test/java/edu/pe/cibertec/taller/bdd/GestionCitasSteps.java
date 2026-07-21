package edu.pe.cibertec.taller.bdd;

import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.pe.cibertec.taller.excepcion.HorarioOcupadoException;
import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.modelo.Mecanico;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GestionCitasSteps {

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;
	private Cita citaResultado;
	private Exception excepcionCapturada;

	@Before
	public void inicializar() {

		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
		when(proveedorFechaHora.ahora()).thenReturn(LocalDateTime.of(2026, 9, 14, 8, 0));

	}

	// TODO: implementar aqui los pasos de los escenarios con
	@Given("un mecanico con especialidad MANTENIMIENTO_LIGERO disponible")
	public void unMecanicoConEspecialidadMantenimientoLigeroDisponible() {

		String zafiroEspecialidad = "MANTENIMIENTO_LIGERO";
		Mecanico mecanico = new Mecanico();
		mecanico.setId(1L);
		mecanico.setNombre("Renzo Anampa");
		mecanico.setEspecialidad(TipoServicio.valueOf(zafiroEspecialidad));

		when(repositorioMecanicos.findById(1L)).thenReturn(Optional.of(mecanico));
		when(repositorioCitas.findByMecanicoIdAndEstado(1L, EstadoCita.PROGRAMADA)).thenReturn(List.of());
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

	}

	@When("se agenda un MANTENIMIENTO_LIGERO para la placa {string} el 15 de setiembre de 2026 a las 09:00")
	public void seAgendaUnMantenimientoLigero(String placa) {

		citaResultado = servicioCitas.agendarCita(1L, placa, TipoServicio.MANTENIMIENTO_LIGERO,
				LocalDateTime.of(2026, 9, 15, 9, 0));

	}

	@Then("la cita queda en estado PROGRAMADA")
	public void laCitaQuedaEnEstadoProgramada() {

		assertEquals(EstadoCita.PROGRAMADA, citaResultado.getEstado());

	}

	@Then("se notifica el agendamiento")
	public void seNotificaElAgendamiento() {

		verify(servicioNotificaciones, times(1)).notificarCitaAgendada(any(Cita.class));

	}

	@Given("un mecanico que ya tiene una cita programada de 10:00 a 12:00 el 15 de setiembre de 2026")
	public void unMecanicoQueYaTieneUnaCitaProgramada() {

		Mecanico mecanico = new Mecanico();
		mecanico.setId(2L);
		mecanico.setEspecialidad(TipoServicio.MANTENIMIENTO_LIGERO);

		Cita citaExistente = new Cita();
		citaExistente.setFechaHoraInicio(LocalDateTime.of(2026, 9, 15, 10, 0));
		citaExistente.setDuracionHoras(2);
		citaExistente.setEstado(EstadoCita.PROGRAMADA);

		when(repositorioMecanicos.findById(2L)).thenReturn(Optional.of(mecanico));
		when(repositorioCitas.findByMecanicoIdAndEstado(2L, EstadoCita.PROGRAMADA)).thenReturn(List.of(citaExistente));
		when(repositorioCitas.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

	}

	@When("se intenta agendar una nueva cita con ese mecanico a las {int}:00")
	public void seIntentaAgendarUnaNuevaCitaConEseMecanico(int hora) {

		try {
			citaResultado = servicioCitas.agendarCita(2L, "ANA-525", TipoServicio.MANTENIMIENTO_LIGERO,
					LocalDateTime.of(2026, 9, 15, hora, 0));
			excepcionCapturada = null;
		} catch (Exception e) {
			excepcionCapturada = e;
		}

	}

	@Then("se rechaza el agendamiento por horario ocupado")
	public void seRechazaElAgendamientoPorHorarioOcupado() {

		assertEquals(HorarioOcupadoException.class, excepcionCapturada.getClass());

	}

	@Then("la cita se agenda correctamente porque no hay superposicion")
	public void laCitaSeAgendaCorrectamentePorqueNoHaySuperposicion() {

		assertEquals(EstadoCita.PROGRAMADA, citaResultado.getEstado());

	}
}
