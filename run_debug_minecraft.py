import os
import sys
import time
import subprocess
import shlex
import glob

VERSION_DIR = r"E:\Program Files (x86)\Marcft\.minecraft\versions\1.20.1 E7"
CRASH_DIR = os.path.join(VERSION_DIR, "crash-reports")
LOG_PATH = os.path.join(VERSION_DIR, "logs", "latest.log")
DEBUG_LOG_PATH = os.path.join(VERSION_DIR, "logs", "debug.log")

def log(msg):
    print(msg, flush=True)

def get_crash_reports():
    if not os.path.exists(CRASH_DIR):
        return []
    return glob.glob(os.path.join(CRASH_DIR, "*.txt"))

def read_log_incremental(path, current_pos):
    if not os.path.exists(path):
        return [], current_pos
    try:
        size = os.path.getsize(path)
        # Handle file rotation or truncation by Log4j on startup
        if size < current_pos:
            current_pos = 0
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            f.seek(current_pos)
            lines = f.readlines()
            current_pos = f.tell()
            return lines, current_pos
    except Exception:
        return [], current_pos

def main():
    log("=" * 60)
    log("MCI Minecraft Launch & Debug Verification Script")
    log("=" * 60)

    if not os.path.exists("raw_command.txt"):
        log("Error: raw_command.txt not found!")
        sys.exit(1)

    with open("raw_command.txt", "r", encoding="utf-8") as f:
        cmd_str = f.read().strip()

    # Tokenize arguments for Windows and strip outer quotes
    cmd_args = shlex.split(cmd_str, posix=False)
    cmd_args = [a[1:-1] if a.startswith('"') and a.endswith('"') else a for a in cmd_args]
    log(f"Command binary: {cmd_args[0]}")
    log(f"Binary exists: {os.path.exists(cmd_args[0])}")
    log(f"Total arguments: {len(cmd_args)}")

    initial_crashes = set(get_crash_reports())
    log(f"Initial crash reports count: {len(initial_crashes)}")

    # Initial log positions (will auto-reset to 0 once log4j rotates files)
    latest_pos = os.path.getsize(LOG_PATH) if os.path.exists(LOG_PATH) else 0
    debug_pos = os.path.getsize(DEBUG_LOG_PATH) if os.path.exists(DEBUG_LOG_PATH) else 0
    log(f"Initial latest.log position: {latest_pos} bytes")
    log(f"Initial debug.log position: {debug_pos} bytes")

    log("\nStarting Minecraft process...")
    proc = subprocess.Popen(
        cmd_args,
        cwd=VERSION_DIR,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="gb18030",
        errors="replace",
        bufsize=1
    )

    log(f"Process PID: {proc.pid}")

    mod_discovered = False
    mod_constructed = False
    common_setup_done = False
    client_setup_done = False
    game_loaded = False
    crashed = False
    crash_reason = ""

    start_time = time.time()
    timeout = 180  # 3 minutes maximum timeout for launch

    def check_line(line_clean):
        nonlocal mod_discovered, mod_constructed, common_setup_done, client_setup_done, game_loaded, crashed, crash_reason
        if not line_clean:
            return

        if any(k in line_clean for k in [
            "mekanism_complex_industries",
            "Mekanism Complex Industries",
            "MCI",
            "CrashReport",
            "FATAL",
            "NoSuchFieldError",
            "Failed to create mod instance",
            "OpenAL initialized",
            "Setting user",
            "Game took",
            "Sound engine started"
        ]):
            log(f"[LOG] {line_clean}")

        if "mekanism_complex_industries-1.0.0.jar" in line_clean:
            mod_discovered = True
        if "Initializing Mekanism Complex Industries" in line_clean:
            mod_constructed = True
        if "common setup completed" in line_clean:
            common_setup_done = True
        if "client setup" in line_clean:
            client_setup_done = True
        if "Game took" in line_clean or "OpenAL initialized" in line_clean or "Sound engine started" in line_clean:
            game_loaded = True

        if "NoSuchFieldError" in line_clean or "Failed to create mod instance" in line_clean or "Mod Loading has failed" in line_clean:
            crashed = True
            crash_reason = line_clean

    try:
        while True:
            # Check if process died
            ret = proc.poll()
            if ret is not None:
                log(f"\n[INFO] Process exited with return code: {ret}")
                break

            # Read stdout line if available
            line = proc.stdout.readline()
            if line:
                check_line(line.strip())

            # Read new lines from latest.log
            new_lines, latest_pos = read_log_incremental(LOG_PATH, latest_pos)
            for nl in new_lines:
                check_line(nl.strip())

            # Read new lines from debug.log
            new_debug_lines, debug_pos = read_log_incremental(DEBUG_LOG_PATH, debug_pos)
            for ndl in new_debug_lines:
                check_line(ndl.strip())

            # Check new crash reports
            current_crashes = set(get_crash_reports())
            new_crashes = current_crashes - initial_crashes
            if new_crashes:
                crashed = True
                crash_file = list(new_crashes)[0]
                log(f"\n[CRASH REPORT CREATED] {crash_file}")
                with open(crash_file, "r", encoding="utf-8", errors="ignore") as cf:
                    log(cf.read()[:2000])
                break

            # Success condition: Game successfully reached post-init / main menu
            elapsed = time.time() - start_time
            if game_loaded and client_setup_done and not crashed and elapsed > 50:
                log("\n" + "=" * 60)
                log(f"[SUCCESS] Game reached main menu and has been running stably for {elapsed:.1f}s!")
                log(f"Mod discovered: {mod_discovered}")
                log(f"Mod constructed: {mod_constructed}")
                log(f"Common setup: {common_setup_done}")
                log(f"Client setup: {client_setup_done}")
                log("=" * 60)
                break

            if elapsed > timeout:
                log(f"\n[TIMEOUT] Exceeded {timeout} seconds.")
                break

            time.sleep(0.1)

    finally:
        if proc.poll() is None:
            log("[INFO] Terminating Minecraft process cleanly...")
            proc.terminate()
            try:
                proc.wait(timeout=10)
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.wait()
            log("[INFO] Process terminated.")

    # Final verdict
    current_crashes = set(get_crash_reports())
    new_crashes = current_crashes - initial_crashes
    if new_crashes:
        log(f"\nFAILED: New crash report generated: {new_crashes}")
        sys.exit(1)
    elif crashed:
        log(f"\nFAILED: Crash detected: {crash_reason}")
        sys.exit(1)
    elif not game_loaded:
        log(f"\nFAILED: Game did not finish loading within timeout.")
        sys.exit(1)
    else:
        log("\nALL VERIFICATIONS PASSED! Minecraft launched successfully without crashing.")
        sys.exit(0)

if __name__ == "__main__":
    main()
