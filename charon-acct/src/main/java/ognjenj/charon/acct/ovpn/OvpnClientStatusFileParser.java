package ognjenj.charon.acct.ovpn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ognjenj.charon.acct.exceptions.GenericAccountingException;
import ognjenj.charon.acct.exceptions.OvpnClientStatusFileNotFoundException;
import ognjenj.charon.acct.util.VariableDateParser;

public class OvpnClientStatusFileParser {
	public static Map<String, OvpnClientStatus> parseClientStatusFile(String path)
			throws OvpnClientStatusFileNotFoundException, GenericAccountingException, IOException {
		if (path == null) {
			throw new OvpnClientStatusFileNotFoundException("Error opening the status file. Path is null.");
		}
		Map<String, OvpnClientStatus> clients = new HashMap<>();
		File originalFile = new File(path);
		if (!originalFile.exists() || !originalFile.isFile()) {
			throw new OvpnClientStatusFileNotFoundException("The OVPN status file doesn't exist.");
		}
		Path originalPath = originalFile.toPath();
		Path tmpStatusFilePath = Paths.get(System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString());
		Files.copy(originalPath, tmpStatusFilePath, StandardCopyOption.REPLACE_EXISTING);
		BufferedReader reader = new BufferedReader(new FileReader(tmpStatusFilePath.toFile()));
		String line;
		boolean startedProcessing = false;
		while ((line = reader.readLine()) != null) {
			if (line.trim().startsWith("Common Name,Real Address,")) {
				startedProcessing = true;
			} else if (startedProcessing && line.trim().startsWith("ROUTING TABLE")) {
				break;
			} else if (startedProcessing) {
				try {
					String[] elements = line.trim().split(",");
					clients.put(elements[0], new OvpnClientStatus(elements[0], elements[1], Long.parseLong(elements[2]),
							Long.parseLong(elements[3]),
							VariableDateParser.parseDateTimeWithVariablePadding(elements[4], "EEE LLL d H:mm:ss yyyy")));
				} catch (DateTimeParseException ex) {
					Files.deleteIfExists(tmpStatusFilePath);
					throw new GenericAccountingException(ex.getMessage(), ex);
				}
			}
		}
		Files.delete(tmpStatusFilePath);
		return clients;
	}
}
