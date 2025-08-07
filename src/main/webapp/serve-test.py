#!/usr/bin/env python3
"""
Simple HTTP server for serving frontend files with real backend integration
"""

import http.server
import socketserver
from pathlib import Path

PORT = 3001
ROOT_DIR = Path(__file__).parent

class StaticFileHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(ROOT_DIR), **kwargs)
    
    def end_headers(self):
        # Add CORS headers for frontend-backend communication
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        super().end_headers()
    
    def do_GET(self):
        if self.path == '/':
            self.path = '/test-login.html'
        super().do_GET()
    
    def log_message(self, format, *args):
        print(f"{self.command} {self.path}")

def main():
    try:
        with socketserver.TCPServer(("", PORT), StaticFileHandler) as httpd:
            print(f"\n🚀 Frontend Test Server Started!")
            print(f"📍 Server running at: http://localhost:{PORT}")
            print(f"📁 Serving files from: {ROOT_DIR}")
            print(f"🔗 Test login page: http://localhost:{PORT}/test-login.html")
            print(f"\n🔌 Backend API: http://localhost:8080/api/v1")
            print(f"📖 API Documentation: http://localhost:8080/swagger-ui.html")
            print(f"\n💡 Use Ctrl+C to stop the server\n")
            
            httpd.serve_forever()
            
    except KeyboardInterrupt:
        print("\n👋 Server stopped by user")
    except OSError as e:
        if e.errno == 98:  # Address already in use
            print(f"❌ Port {PORT} is already in use. Please try a different port.")
        else:
            print(f"❌ Server error: {e}")

if __name__ == "__main__":
    main()