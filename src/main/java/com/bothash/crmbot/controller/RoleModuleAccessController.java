package com.bothash.crmbot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bothash.crmbot.entity.RoleModuleAccess;
import com.bothash.crmbot.entity.Modules;
import com.bothash.crmbot.service.RoleModuleAccessService;

@RestController
@RequestMapping("/role-access")
public class RoleModuleAccessController {

    @Autowired
    private RoleModuleAccessService accessService;

    // ✅ Save or Update Access
    @PostMapping
    public ResponseEntity<RoleModuleAccess> saveAccess(@RequestBody RoleModuleAccess access) {
        RoleModuleAccess saved = accessService.save(access);
        return ResponseEntity.ok(saved);
    }

    // ✅ Get all access mappings
    @GetMapping
    public ResponseEntity<List<RoleModuleAccess>> getAllAccess() {
        return ResponseEntity.ok(accessService.getAll());
    }

    // ✅ Get access by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<RoleModuleAccess>> getAccessByRole(@PathVariable String role) {
        return ResponseEntity.ok(accessService.getByRole(role));
    }

    // ✅ Check if role has access to a module
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAccess(@RequestParam String role, @RequestParam Long moduleId) {
        Modules module = new Modules();
        module.setId(moduleId); // You only need to set the ID to use in comparison
        boolean hasAccess = accessService.hasAccess(role, module);
        return ResponseEntity.ok(hasAccess);
    }

    // ✅ Delete all access by role
    @DeleteMapping("/role/{role}")
    public ResponseEntity<String> deleteAccessByRole(@PathVariable String role) {
        accessService.deleteAccessByRole(role);
        return ResponseEntity.ok("Access removed for role: " + role);
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<String> saveBulkAccess(@RequestBody List<RoleModuleAccess> accessList) {
    	 accessService.saveBulk(accessList);
    	 return ResponseEntity.ok("Access saved/updated successfully.");
    }
}
