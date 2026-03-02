package dev.jeatog.comala.api.servicios;

import dev.jeatog.comala.negocio.constantes.Constantes;
import dev.jeatog.comala.negocio.excepcion.ComalaExcepcion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class AlmacenamientoServicio {

    private final Path directorioBase;
    private final String urlBase;

    public AlmacenamientoServicio(
            @Value("${comala.almacenamiento.directorio}") String directorio,
            @Value("${comala.almacenamiento.url-base}") String urlBase
    ) throws IOException {
        this.directorioBase = Paths.get(directorio).toAbsolutePath().normalize();
        this.urlBase = urlBase;
        Files.createDirectories(this.directorioBase);
    }

    public String guardar(MultipartFile archivo) {
        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = directorioBase.resolve(nombreArchivo);
        try {
            Files.copy(archivo.getInputStream(), destino);
        } catch (IOException e) {
            throw new ComalaExcepcion(
                    Constantes.ERR_ALMACENAMIENTO,
                    "No se pudo guardar el archivo.",
                    Constantes.HTTP_500_INTERNAL_ERROR
            );
        }
        return urlBase + "/" + nombreArchivo;
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
    }
}
