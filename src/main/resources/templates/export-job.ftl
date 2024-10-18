apiVersion: batch/v1
kind: Job
metadata:
  name: ${jobName}
  namespace: ${namespace}
spec:
  backoffLimit: 2
  template:
    spec:
      restartPolicy: Never
      serviceAccountName:  data-export-secret-manager
      containers:
        - name: ${containerName}
          image: ${image}
          resources:
            requests:
              memory: 2G
            limits:
              memory: 2G
          env:
            - name: spring.profiles.active
              value: ${jobType}
            - name: job.input-fields
              value: ${inputFields}
            - name: job.input-values
              value: ${inputValues}
            - name: job.id
              value: ${jobId}
            - name: job.target-type
              value: ${targetType}
            - name: endpoint.backend
              value: https://dev.dissco.tech/api/v1/export/internal
            - name: endpoint.token
              value: https://login-demo.dissco.eu/auth/realms/dissco/protocol/openid-connect/token
            - name: token.grant-type
              value: client_credentials
            - name: token.id
              value: demo-api-client
            - name: token.secret
              valueFrom:
                secretKeyRef:
                  name: aws-secrets
                  key: handle-endpoint-token
            - name: elasticsearch.hostname
              value: elastic-search-es-http.elastic.svc.cluster.local
            - name: elasticsearch.port
              value: "9200"
            - name: elasticsearch.username
              valueFrom:
                secretKeyRef:
                  name: aws-secrets
                  key: elastic-username
            - name: elasticsearch.password
              valueFrom:
                secretKeyRef:
                  name: aws-secrets
                  key: elastic-password
          securityContext:
            runAsNonRoot: true
            allowPrivilegeEscalation: false
          volumeMounts:
            - mountPath: /temp
              name: temp-volume
            - name: db-secrets
              mountPath: "/mnt/secrets-store/db-secrets"
              readOnly: true
      volumes:
        - name: temp-volume
          emptyDir: { }
        - name: aws-secrets
          csi:
            driver: secrets-store.csi.k8s.io
            readOnly: true
            volumeAttributes:
              secretProviderClass: "aws-secrets"
