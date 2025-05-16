package com.proyecto.galeria.service;

import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.reporte;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.ReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class ReporteService {

    public Optional<reporte> findSent(Integer id){
        return repo.findById(id)
                .filter(r -> r.getEstado() == reporte.Estado.SENT);
    }

    @Autowired private ReporteRepository repo;
    @Autowired private albumService albumService;
    @Autowired private IUsuarioService usuarioService;

    public reporte saveDraft(Integer albumId,
                             Integer userId,
                             String contenido) {

        album a  = albumService.get(albumId).orElseThrow();
        usuario u = usuarioService.findById(userId).orElseThrow();

        reporte r = new reporte();
        r.setAlbum(a);
        r.setAutor(u);
        r.setContenido(contenido);
        r.setFechaHora(LocalDateTime.now());
        r.setEstado(reporte.Estado.DRAFT);

        return repo.save(r);
    }

    public reporte send(Integer albumId,
                        Integer userId,
                        String contenido) {

        album a  = albumService.get(albumId).orElseThrow();
        usuario u = usuarioService.findById(userId).orElseThrow();

        reporte r = new reporte();
        r.setAlbum(a);
        r.setAutor(u);
        r.setContenido(contenido);
        r.setFechaHora(LocalDateTime.now());
        r.setEstado(reporte.Estado.SENT);

        return repo.save(r);
    }

    public List<reporte> logs(Integer albumId) {
        return repo.findByAlbumAndEstadoOrderByFechaHoraDesc(
                albumService.get(albumId).orElseThrow(),
                reporte.Estado.SENT);                     // ← only SENT
    }

    public reporte update(Integer reportId, String contenido) {
        reporte r = repo.findById(reportId).orElseThrow();
        r.setContenido(contenido);
        return repo.save(r);
    }

    public List<reporte> allSent() {
        return repo.findByEstadoOrderByFechaHoraDesc(reporte.Estado.SENT);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

