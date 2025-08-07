/**
 * Simple HTTP server for testing the frontend without backend
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const ROOT_DIR = __dirname;

// MIME types
const mimeTypes = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

function getMimeType(filePath) {
    const ext = path.extname(filePath).toLowerCase();
    return mimeTypes[ext] || 'application/octet-stream';
}

function serveFile(res, filePath) {
    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('File not found');
            return;
        }
        
        const mimeType = getMimeType(filePath);
        res.writeHead(200, { 
            'Content-Type': mimeType,
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization'
        });
        res.end(data);
    });
}

function mockApiResponse(res, endpoint, method) {
    res.writeHead(200, { 
        'Content-Type': 'application/json',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization'
    });
    
    // Mock API responses for testing
    if (endpoint === '/api/v1/auth/login' && method === 'POST') {
        // Mock successful login
        const mockResponse = {
            token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJ0ZXN0dXNlciIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlVTRVIiXSwicGVybWlzc2lvbnMiOlsiUkVBRF9TVFVERU5UUyJdLCJpYXQiOjE2NDA5OTUyMDAsImV4cCI6MTY0MDk5ODgwMH0.signature',
            refreshToken: 'mock-refresh-token-123',
            user: {
                id: 1,
                username: 'testuser',
                email: 'test@example.com',
                firstName: 'Test',
                lastName: 'User',
                roles: ['USER'],
                permissions: ['READ_STUDENTS']
            }
        };
        res.end(JSON.stringify(mockResponse));
    } else if (endpoint === '/api/v1/auth/logout' && method === 'POST') {
        res.end(JSON.stringify({ message: 'Logged out successfully' }));
    } else if (endpoint === '/api/v1/auth/refresh' && method === 'POST') {
        const mockResponse = {
            token: 'new-jwt-token-123',
            refreshToken: 'new-refresh-token-123'
        };
        res.end(JSON.stringify(mockResponse));
    } else {
        res.writeHead(404);
        res.end(JSON.stringify({ error: 'API endpoint not found' }));
    }
}

const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url, true);
    const pathname = parsedUrl.pathname;
    
    console.log(`${req.method} ${pathname}`);
    
    // Handle CORS preflight
    if (req.method === 'OPTIONS') {
        res.writeHead(200, {
            'Access-Control-Allow-Origin': '*',
            'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
            'Access-Control-Allow-Headers': 'Content-Type, Authorization'
        });
        res.end();
        return;
    }
    
    // Handle API requests
    if (pathname.startsWith('/api/')) {
        mockApiResponse(res, pathname, req.method);
        return;
    }
    
    // Handle file requests
    let filePath = path.join(ROOT_DIR, pathname === '/' ? 'test-login.html' : pathname);
    
    // Security check - prevent directory traversal
    if (!filePath.startsWith(ROOT_DIR)) {
        res.writeHead(403, { 'Content-Type': 'text/plain' });
        res.end('Forbidden');
        return;
    }
    
    // Check if file exists
    fs.stat(filePath, (err, stats) => {
        if (err || !stats.isFile()) {
            // Try with .html extension
            if (!path.extname(filePath)) {
                filePath += '.html';
                fs.stat(filePath, (err, stats) => {
                    if (err || !stats.isFile()) {
                        res.writeHead(404, { 'Content-Type': 'text/plain' });
                        res.end('File not found');
                        return;
                    }
                    serveFile(res, filePath);
                });
                return;
            }
            
            res.writeHead(404, { 'Content-Type': 'text/plain' });
            res.end('File not found');
            return;
        }
        
        serveFile(res, filePath);
    });
});

server.listen(PORT, () => {
    console.log(`\n🚀 Frontend Test Server Started!`);
    console.log(`📍 Server running at: http://localhost:${PORT}`);
    console.log(`📁 Serving files from: ${ROOT_DIR}`);
    console.log(`🔗 Test login page: http://localhost:${PORT}/test-login.html`);
    console.log(`\n📝 Mock API endpoints available:`);
    console.log(`   POST /api/v1/auth/login - Mock login`);
    console.log(`   POST /api/v1/auth/logout - Mock logout`);
    console.log(`   POST /api/v1/auth/refresh - Mock token refresh`);
    console.log(`\n💡 Use Ctrl+C to stop the server\n`);
});

server.on('error', (err) => {
    if (err.code === 'EADDRINUSE') {
        console.error(`❌ Port ${PORT} is already in use. Please try a different port.`);
    } else {
        console.error('❌ Server error:', err);
    }
});