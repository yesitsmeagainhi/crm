package com.bothash.crmbot.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bothash.crmbot.entity.Platforms;
import com.bothash.crmbot.service.PlatformService;

@RestController
@RequestMapping("/source/")
public class SourceController {
	
	@Autowired
	private PlatformService platformsService;
	
	@GetMapping("/getall")
	public ResponseEntity<List<Platforms>> getAll(){
		List<Platforms> platfroms=this.platformsService.getAll();
		return new ResponseEntity<List<Platforms>>(platfroms,HttpStatus.OK);
	}
	
	// GET platform by ID
    @GetMapping("/{id}")
    public ResponseEntity<Platforms> getById(@PathVariable Long id) {
        Platforms platform = platformsService.getById(id);
        return platform!=null?ResponseEntity.ok().body(platform):ResponseEntity.notFound().build();
    }

    // CREATE platform
    @PostMapping("/create")
    public ResponseEntity<Platforms> create(@RequestBody Platforms platform) {
        return ResponseEntity.status(201).body(platformsService.create(platform));
    }

    // UPDATE platform
    @PutMapping("/{id}")
    public ResponseEntity<Platforms> update(@PathVariable Long id, @RequestBody Platforms platform) {
        Optional<Platforms> updated = platformsService.update(id, platform);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // DELETE platform
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = platformsService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
