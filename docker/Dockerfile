FROM universalresolver/base-ubuntu

# build driver-did-echo
ENV uniresolver_driver_did_echo_mainnet_rpc_url=http://127.0.0.1:8090/rpc
ENV uniresolver_driver_did_echo_testnet_rpc_url=http://127.0.0.1:8090/rpc
ENV uniresolver_driver_did_echo_devnet_rpc_url=http://127.0.0.1:8090/rpc
ADD . /opt/driver-did-echo
RUN cd /opt/driver-did-echo && mvn clean install package -N -DskipTests

# done
EXPOSE 8080

RUN chmod a+rx /opt/driver-did-echo/docker/run-driver-did-echo.sh
CMD "/opt/driver-did-echo/docker/run-driver-did-echo.sh"
    