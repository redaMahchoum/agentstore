package com.agentstore.userservice.model.mapper;

import com.agentstore.userservice.model.dto.UserDTO;
import com.agentstore.userservice.model.entity.User;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-28T18:08:57+0000",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.11 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.id( user.getId() );
        userDTO.keycloakId( user.getKeycloakId() );
        userDTO.username( user.getUsername() );
        userDTO.firstName( user.getFirstName() );
        userDTO.lastName( user.getLastName() );
        userDTO.email( user.getEmail() );
        userDTO.enabled( user.isEnabled() );
        userDTO.emailVerified( user.isEmailVerified() );
        Set<String> set = user.getRoles();
        if ( set != null ) {
            userDTO.roles( new LinkedHashSet<String>( set ) );
        }
        userDTO.createdAt( user.getCreatedAt() );
        userDTO.updatedAt( user.getUpdatedAt() );

        return userDTO.build();
    }

    @Override
    public List<UserDTO> toDTOList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserDTO> list = new ArrayList<UserDTO>( users.size() );
        for ( User user : users ) {
            list.add( toDTO( user ) );
        }

        return list;
    }

    @Override
    public User toEntity(UserDTO userDTO) {
        if ( userDTO == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.keycloakId( userDTO.getKeycloakId() );
        user.username( userDTO.getUsername() );
        user.firstName( userDTO.getFirstName() );
        user.lastName( userDTO.getLastName() );
        user.email( userDTO.getEmail() );
        user.enabled( userDTO.isEnabled() );
        user.emailVerified( userDTO.isEmailVerified() );
        Set<String> set = userDTO.getRoles();
        if ( set != null ) {
            user.roles( new LinkedHashSet<String>( set ) );
        }

        return user.build();
    }

    @Override
    public void updateUserEntity(UserDTO userDTO, User user) {
        if ( userDTO == null ) {
            return;
        }

        user.setUsername( userDTO.getUsername() );
        user.setFirstName( userDTO.getFirstName() );
        user.setLastName( userDTO.getLastName() );
        user.setEmail( userDTO.getEmail() );
        user.setEnabled( userDTO.isEnabled() );
        user.setEmailVerified( userDTO.isEmailVerified() );
        if ( user.getRoles() != null ) {
            Set<String> set = userDTO.getRoles();
            if ( set != null ) {
                user.getRoles().clear();
                user.getRoles().addAll( set );
            }
            else {
                user.setRoles( null );
            }
        }
        else {
            Set<String> set = userDTO.getRoles();
            if ( set != null ) {
                user.setRoles( new LinkedHashSet<String>( set ) );
            }
        }
    }
}
