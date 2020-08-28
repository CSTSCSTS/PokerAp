package com.example.demo.dto;

import com.example.demo.domain.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

	private String roleName;
	private int roleNumber;

	public static RoleDto convertRoleDto(Role role) {
			return RoleDto.builder()
							.roleName(role.getRoleName().toString())
							.roleNumber(role.getRoleNumber())
							.build();
	}

}
