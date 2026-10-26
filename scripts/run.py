"""Run a suite against the real Demo Users API or an explicitly configured service."""
import argparse
import json
import os
from pathlib import Path
import secrets
import socket
import subprocess
import sys
import tempfile
import time
import urllib.request

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--local-api', action='store_true')
    parser.add_argument('command', nargs=argparse.REMAINDER)
    args = parser.parse_args()
    command = args.command[1:] if args.command[:1] == ['--'] else args.command
    if not command:
        parser.error('provide a command after --')
    env = os.environ.copy()
    env.setdefault('PARABANK_URL', 'https://parabank.parasoft.com/parabank')
    server = None
    service_directory = None
    key = env.get('DEMO_API_KEY', '')
    try:
        if args.local_api:
            key = secrets.token_urlsafe(40)
            with socket.socket() as listener:
                listener.bind(('127.0.0.1', 0))
                port = listener.getsockname()[1]
            env['DEMO_BASE_URL'] = f'http://127.0.0.1:{port}'
            env['DEMO_API_KEY'] = key
            service_env = {**env, 'API_KEYS': json.dumps({'automation': key})}
            service_directory = tempfile.TemporaryDirectory(prefix='demo-users-api-')
            server = subprocess.Popen(
                [sys.executable, '-m', 'uvicorn', 'demo_api.main:create_app',
                 '--factory', '--host', '127.0.0.1', '--port', str(port),
                 '--no-access-log', '--log-level', 'warning'],
                env=service_env, cwd=service_directory.name,
                stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            deadline = time.monotonic() + 30
            while True:
                if server.poll() is not None:
                    raise RuntimeError('Demo Users API stopped during startup')
                try:
                    with urllib.request.urlopen(env['DEMO_BASE_URL'] + '/health', timeout=1) as response:
                        health = json.load(response)
                    if health == {'status': 'ok', 'storage': 'temporary'}:
                        break
                except (OSError, ValueError):
                    pass
                if time.monotonic() >= deadline:
                    raise RuntimeError('Demo Users API did not become healthy')
                time.sleep(0.1)
        if key and env.get('GITHUB_ACTIONS'):
            print('::add-mask::' + key, flush=True)
        # Resolve Windows .cmd launchers without invoking a shell.
        import shutil
        command[0] = shutil.which(command[0]) or command[0]
        process = subprocess.Popen(command, env=env, stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT, text=True, encoding='utf-8', errors='replace')
        for line in process.stdout:
            print(line.replace(key, '[REDACTED]') if key else line, end='')
        status = process.wait()
        if key:
            for folder in ('reports', 'test-results', 'playwright-report', 'target', 'results'):
                if not Path(folder).exists():
                    continue
                for path in Path(folder).rglob('*'):
                    if path.is_file() and path.suffix.lower() in ('.html', '.json', '.xml', '.txt', '.log', '.csv'):
                        data = path.read_bytes()
                        if key.encode() in data:
                            path.write_bytes(data.replace(key.encode(), b'[REDACTED]'))
        return status
    finally:
        if server is not None:
            server.terminate()
            try:
                server.wait(timeout=10)
            except subprocess.TimeoutExpired:
                server.kill()
                server.wait()
        if service_directory is not None:
            service_directory.cleanup()

if __name__ == '__main__':
    raise SystemExit(main())
