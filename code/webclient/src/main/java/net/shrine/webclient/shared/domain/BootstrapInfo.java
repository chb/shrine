package net.shrine.webclient.shared.domain;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * @author clint
 * @date Sep 4, 2012
 */
public final class BootstrapInfo {
    @JsonProperty
    private final String loggedInUsername;

    @JsonCreator
    public BootstrapInfo(@JsonProperty("loggedInUsername") final String loggedInUsername) {
        super();

        this.loggedInUsername = loggedInUsername;
    }

    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (loggedInUsername == null ? 0 : loggedInUsername.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BootstrapInfo other = (BootstrapInfo) obj;
        if (loggedInUsername == null) {
            if (other.loggedInUsername != null) {
                return false;
            }
        } else if (!loggedInUsername.equals(other.loggedInUsername)) {
            return false;
        }
        return true;
    }
}
