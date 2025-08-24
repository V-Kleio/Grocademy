class AuthUtils {
    static checkAuthAndRedirect() {
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
            const currentPath = window.location.pathname;
            if (currentPath !== '/auth' && currentPath !== '/login' && !currentPath.startsWith('/auth')) {
                window.location.href = '/auth?error=session_expired';
            }
            return true;
        }
        return false;
    }

    static checkCurrentPageAuth() {
        const protectedPaths = ['/dashboard', '/courses', '/my-courses'];
        const currentPath = window.location.pathname;

        if (protectedPaths.some(path => currentPath.startsWith(path))) {
            return true;
        }
        return false;
    }

    static getAuthToken() {
        return localStorage.getItem('jwt_token');
    }
}

window.logout = AuthUtils.logout;
window.getAuthToken = AuthUtils.getAuthToken;

document.addEventListener('DOMContentLoaded', function() {
    const currentPath = window.location.pathname;
    if (!currentPath.startsWith('/auth') && !currentPath.startsWith('/login')) {
        AuthUtils.checkCurrentPageAuth();
    }
});