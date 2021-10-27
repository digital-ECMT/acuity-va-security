/*
 * Copyright 2021 The University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acuity.va.security.auth.azure;

import com.acuity.va.security.auth.common.AjaxInvalidSessionStrategy;
import com.acuity.va.security.auth.common.OAuth2ClientAuthenticationWithProviderProcessingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.InMemoryClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Profile("azure-sso")
@Configuration
@EnableOAuth2Client
public class AzureSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

    @Qualifier("remoteAuthenticationProvider")
    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private OAuth2ClientContext clientContext;

    @Value("${azure.logoutUrl:/}")
    private String logoutUrl;

    @Bean
    public FilterRegistrationBean contextFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl(logoutUrl)
                .invalidateHttpSession(true)
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class)
                .sessionManagement().invalidSessionUrl("/")
                .invalidSessionStrategy(new AjaxInvalidSessionStrategy("/", true))
                .maximumSessions(4).expiredUrl("/")
                .and().and()
                .exceptionHandling().accessDeniedPage("/403.html");
    }

    @Bean
    @ConfigurationProperties("azure.client")
    public ClientDetails clientDetails() {
        return new BaseClientDetails();
    }

    @Bean
    @ConfigurationProperties("azure.resource")
    public AuthorizationCodeResourceDetails resourceDetails() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    public Filter ssoFilter() throws Exception {
        OAuth2ClientAuthenticationWithProviderProcessingFilter filter = new OAuth2ClientAuthenticationWithProviderProcessingFilter("/login");

        filter.setAuthenticationProvider(authenticationProvider);
        filter.setRestTemplate(restTemplate());
        filter.setTokenServices(tokenServices());

        return filter;
    }

    private AccessTokenProvider accessTokenProvider() {
        AzureAccessTokenProvider authorizationCodeAccessTokenProvider = new AzureAccessTokenProvider();
        authorizationCodeAccessTokenProvider.setTokenRequestEnhancer(new AzureRequestEnhancer());

        ImplicitAccessTokenProvider implicitAccessTokenProvider = new ImplicitAccessTokenProvider();
        ResourceOwnerPasswordAccessTokenProvider resourceOwnerPasswordAccessTokenProvider = new ResourceOwnerPasswordAccessTokenProvider();
        ClientCredentialsAccessTokenProvider clientCredentialsAccessTokenProvider = new ClientCredentialsAccessTokenProvider();

        return new AccessTokenProviderChain(Arrays.<AccessTokenProvider>asList(
                authorizationCodeAccessTokenProvider,
                implicitAccessTokenProvider,
                resourceOwnerPasswordAccessTokenProvider,
                clientCredentialsAccessTokenProvider));
    }

    private JwtTokenStore tokenStore() {
        DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
        DefaultUserAuthenticationConverter userTokenConverter = new DefaultUserAuthenticationConverter();
        tokenConverter.setUserTokenConverter(userTokenConverter);
        AzureJwtAccessTokenConverter jwtTokenEnhancer = new AzureJwtAccessTokenConverter();
        jwtTokenEnhancer.setAccessTokenConverter(tokenConverter);
        return new JwtTokenStore(jwtTokenEnhancer);
    }

    private InMemoryClientDetailsService clientDetailsService() {
        ClientDetails clientDetails = clientDetails();
        Map<String, ClientDetails> detailsMap = new LinkedHashMap<>();
        detailsMap.put(clientDetails.getClientId(), clientDetails);
        InMemoryClientDetailsService clientDetailsService = new InMemoryClientDetailsService();
        clientDetailsService.setClientDetailsStore(detailsMap);
        return clientDetailsService;
    }

    private DefaultTokenServices tokenServices() {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setClientDetailsService(clientDetailsService());
        return tokenServices;
    }

    @Bean
    @Primary
    public OAuth2RestTemplate restTemplate() {
        OAuth2RestTemplate azureTemplate = new OAuth2RestTemplate(
                resourceDetails(),
                clientContext
        );
        azureTemplate.setAccessTokenProvider(accessTokenProvider());
        return azureTemplate;
    }
}
