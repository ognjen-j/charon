package ognjenj.charon.web.controllers;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import ognjenj.charon.web.config.ConfigurationStore;
import ognjenj.charon.web.model.ConnectionAttempt;
import ognjenj.charon.web.model.forms.FilterForm;
import ognjenj.charon.web.repositories.RadPostAuthRepository;

@Controller
public class AttemptsController {
	@Autowired
	RadPostAuthRepository radPostAuthRepository;
	ConfigurationStore store = ConfigurationStore.getInstance();

	@GetMapping("ovpn/attempts")
	public String loadConnectionsInitial(FilterForm filterForm, Model model) {
		LocalDateTime startDateTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
		FilterForm defaultValues = new FilterForm();
		defaultValues.setEndDate(new Date());
		defaultValues.setStartDate(
				Date.from(startDateTime.toLocalDate().atStartOfDay(ZoneId.of(store.getDbTimezone())).toInstant()));
		model.addAttribute("filterForm", defaultValues);
		return loadData(startDateTime, null, model);
	}

	@PostMapping({"ovpn/attempts"})
	public String filterConnections(FilterForm filterForm, Principal principal, Model model) {
		Calendar calendarFrom = Calendar.getInstance();
		calendarFrom.setTime(filterForm.getStartDate());
		Calendar calendarTo = Calendar.getInstance();
		calendarTo.setTime(filterForm.getEndDate());
		LocalDateTime from = LocalDateTime.of(calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH) + 1,
				calendarFrom.get(Calendar.DAY_OF_MONTH), 0, 0);
		LocalDateTime to = LocalDateTime.of(calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH) + 1,
				calendarTo.get(Calendar.DAY_OF_MONTH), 23, 59);
		return loadData(from, to, model);
	}

	private String loadData(LocalDateTime authTimeFrom, LocalDateTime authTimeTo, Model model) {
		List<ConnectionAttempt> attempts = radPostAuthRepository.getByDate(authTimeFrom, authTimeTo).stream()
				.map(ConnectionAttempt::new).collect(Collectors.toList());
		model.addAttribute("attempts", attempts);
		return "ovpn/attempts";
	}
}
