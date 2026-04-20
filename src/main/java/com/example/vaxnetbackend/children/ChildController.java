package com.example.vaxnetbackend.children;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {
    private final ChildServices childServices;

    @GetMapping("/get-all-children")
    public List<Child> getChildren() {
        return childServices.getAllChildren();
    }

    @PostMapping("/add-new-child")
    public ResponseEntity<ChildResponse> addNewChild(@RequestBody ChildRequest childRequest) {
        return childServices.addNewChild(childRequest);
    }

    @GetMapping("/get-children-by-parent-email/{parentEmail}")
    public List<Child> getChildrenByParentEmail(@PathVariable("parentEmail") String parentEmail) {
        return childServices.getChildrenByParentEmail(parentEmail);
    }

    @GetMapping("/get-children-by-parent/{parentId}")
    public List<Child> getChildrenByParent(@PathVariable("parentId") String parentId) {
        return childServices.getChildrenByParent(parentId);
    }
}
