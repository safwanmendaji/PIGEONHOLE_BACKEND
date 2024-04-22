    package com.appbackend.example.AppBackend.entities;


    import com.fasterxml.jackson.annotation.JsonBackReference;
    import jakarta.persistence.*;
    import lombok.*;
    import org.springframework.boot.autoconfigure.domain.EntityScan;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;

    import java.io.Serializable;
    import java.time.Instant;
    import java.time.LocalDateTime;
    import java.util.Collection;
    import java.util.List;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "users")
    @Getter
    public class User implements UserDetails, Serializable {


        //    @GeneratedValue
        @Id
        private Integer id;

        @Column(nullable = false)
        private String firstName;

        @Column(nullable = false)
        private String lastName;

        private Instant loginTimeStamp;

        private Instant otpGeneratedTime;

        private String password;

        private String otp;



        @OneToOne(mappedBy = "user")
        @JsonBackReference
        private RefreshToken refreshToken;

        @Column(unique = true)
        private String email;

        @Column(unique = true)
        private String phoneNumber;
        
        @Column(columnDefinition = "boolean default false",name="is_approved")
        private Boolean isApproved = false;


        @Enumerated(EnumType.STRING)
        private Role role;


    //    public void setId(Integer id) {
    //        this.id = id;
    //    }


        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority(role.name()));
        }

        @Override
        public String getPassword() {
            return password;
        }

    //    public Integer getUserId() {
    //        return id;
    //    }
        public String getEmail() {
            return email;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public String getUsername() {
            return email != null ? email : String.valueOf(phoneNumber);
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
        
        

        public void setRoleByInput(int input){
            if(input==1){
                this.role=Role.ADMIN;
            }
            else if(input==2){
                this.role=Role.USER;
            }
            else {
                throw new IllegalArgumentException("Invalid input. Only 1 (ADMIN) or 2 (USER) are allowed.");
            }
        }

    }
