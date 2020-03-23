FROM universalresolver/base-ubuntu

# build driver-did-echo
ENV DID_ECHO_DRIVER_MAINNET_RPC_URL=http://127.0.0.1:8090/rpc
ENV DID_ECHO_DRIVER_TESTNET_RPC_URL=http://127.0.0.1:8090/rpc
ENV DID_ECHO_DRIVER_DEVNET_RPC_URL=http://127.0.0.1:8090/rpc
ADD . /opt/driver-did-echo
RUN cd /opt/driver-did-echo && mvn clean install package -N -DskipTests

# done
EXPOSE 8080

RUN chmod a+rx /opt/driver-did-echo/docker/run-driver-did-echo.sh
CMD "/opt/driver-did-echo/docker/run-driver-did-echo.sh"
    