package com.antcolonywars.desktop;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Source sent by the IntelliJ template; unknown fields from older templates are ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StudentSubmission(
        String className,
        String source,
        String pairingKey,
        String username,
        String password
) {
}
