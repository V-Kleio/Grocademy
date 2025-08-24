class AuthUtils {
    static checkAuthAndRedirect() {
        // This will be handled by the server-side JWT filter
        // If user is not authenticated, they'll be redirected by Spring Security
        return true;
    }

    static async logout() {
        try {
            const response = await fetch('/logout', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            });

            if (response.ok) {
                window.location.href = '/auth?logout=true';
            } else {
                console.error('Logout failed');
                window.location.href = '/auth?logout=true';
            }
        } catch (error) {
            console.error('Logout error:', error);
            window.location.href = '/auth?logout=true';
        }
    }

    static handleApiError(response) {
        if (response.status === 401) {
            // JWT expired or invalid, redirect to login
            window.location.href = '/auth?error=session_expired';
            return true;
        }
        return false;
    }

    static checkCurrentPageAuth() {
        const protectedPaths = ['/dashboard', '/courses', '/my-courses'];
        const currentPath = window.location.pathname;

        if (protectedPaths.some(path => currentPath.startsWith(path))) {
            // These paths require authentication
            // If we reach here without being redirected, we're authenticated
            return true;
        }
        return false;
    }
}

window.logout = AuthUtils.logout;
document.addEventListener('DOMContentLoaded', function() {
    AuthUtils.checkCurrentPageAuth();
});