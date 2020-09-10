package ognjenj.charon.web.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@ControllerAdvice
public class ErrorController {

	@GetMapping({"error/denied"})
	public String renderAccessDeniedPage() {
		return "error/denied";
	}

	@RequestMapping({"error"})
	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String renderGenericError() {
		return "error/generic";
	}
}
