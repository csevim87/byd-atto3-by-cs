# BYD Atto3 By CS — Camera Probe

Read-only diagnostic Android app for BYD Atto 3 / DiLink camera framework discovery.

## v0.1 scope
- Detect `/system/framework/bmmcamera.jar`
- Reflectively inspect `android.hardware.AVMCamera`, `JNIBMMCamera`, and `BmmCameraInfo`
- Save a local diagnostic log
- No vehicle-control commands
- No remote access, WebRTC, phone pairing, or background services

This first build is intentionally diagnostic-only. Camera opening/preview will be added after the real Atto 3 framework signatures are captured from the vehicle.
