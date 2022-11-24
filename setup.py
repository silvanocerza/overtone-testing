#!/usr/bin/python

import sys

# if sys.version_info < (3, 10):
#     print("Python 3.10 or later is required")
#     sys.exit(1)

import subprocess
import asyncio
import re
from typing import List
from dataclasses import dataclass

REQUIRED_COMMANDS = [
    "aplay",
    "pasuspender",
    "jackd",
    "scsynth"
]

@dataclass
class Hardware:
    card_index: str
    card_id: str
    card_name: str
    device_index: str
    device_id: str
    device_name: str

HARDWARE_REGEX = re.compile(r"^card (\d+): (.+) \[(.+)\], device (\d+): (.+) \[(.+)\]$")


def is_installed(command: str) -> bool:
    res = subprocess.run(["which", command], capture_output=True)
    return res.returncode == 0

def list_hardware() -> List[Hardware]:
    res = subprocess.run(["aplay", "-l"], capture_output=True, text=True)
    res.check_returncode()

    lines = res.stdout.splitlines()
    devices = []
    for l in lines:
        match = HARDWARE_REGEX.match(l)
        if not match:
            continue
        devices.append(Hardware(
            match.group(1),
            match.group(2),
            match.group(3),
            match.group(4),
            match.group(5),
            match.group(6)
        ))
    return devices


def ask_to_pick_hardware(hardwares: List[Hardware]) -> Hardware:
    picked = None
    while not picked:
        print(f"Which devices should jackd use? [0-{len(hardwares)-1}]")
        for i, hw in enumerate(hardwares):
            print(f"  {i}) Card: {hw.card_name}\tDevice: {hw.device_name}")

        s = input(">> ")

        if s == "":
            picked = hardwares[0]
            break

        try:
            picked = hardwares[int(s)]
        except (ValueError, IndexError):
            print(f"Please pick a number between 0 and {len(hardwares)-1}")
    return picked


async def start_jack(hw: Hardware):
    # /usr/bin/pasuspender -- jackd -dalsa -dhw:0 -D -Phw:PCH,3
    card = f"-dhw:{hw.card_index}"
    device = f"-Phw:{hw.card_id},{hw.device_index}"

    command = ["pasuspender", "--", "jackd", "-dalsa", card, "-D", device]
    return await asyncio.create_subprocess_exec(
        command,
        stdin=asyncio.subprocess.PIPE,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.PIPE,
    )


async def start_supercollider():
    # TODO: Handle configs
    return await asyncio.create_subprocess_exec(
        ["scsynth"],
        stdin=asyncio.subprocess.PIPE,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.PIPE,
    )

async def read_stdout(jack_out, sc_out):
    pass

def restart_pulseaudio() -> bool:
    res = subprocess.run(["systemctl", "--user", "restart", "pulseaudio.socket"], capture_output=True)
    return res.returncode == 0

if __name__ == "__main__":
    not_installed = []
    for cmd in REQUIRED_COMMANDS:
        if not is_installed(cmd):
            not_installed.append(cmd)

    if not_installed:
        print("Following commands are not installed, aborting.")
        print(" ".join(not_installed))
        sys.exit(1)



    picked_hw = ask_to_pick_hardware(list_hardware())

    import curses
    win = curses.initscr()
    win.clear()
    curses.endwin()




    # main_loop = asyncio.get_event_loop()

    # jack_proc = start_jack(picked_hw)
    # sc_proc = start_supercollider()


