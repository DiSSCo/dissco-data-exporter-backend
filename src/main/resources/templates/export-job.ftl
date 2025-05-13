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
      serviceAccountName: data-export-job-service-account
      containers:
        - name: dissco-export-job
          image: ${image}
          resources:
            requests:
              memory: 1G
              cpu: "500m"
            limits:
              memory: 1G
              cpu: "500m"
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
            - name: index.temp-file-location
              value: /temp/tmp.csv.gz
            - name: endpoint.backend
              value: ${backendEndpoint}
            - name: endpoint.token
              value: https://login-demo.dissco.eu/auth/realms/dissco/protocol/openid-connect/token
            - name: token.grant-type
              value: client_credentials
            - name: token.id
              value: ${tokenIdName}
            - name: s3.bucket-name
              value: ${bucketName}
            - name: s3.access-key
              valueFrom:
                secretKeyRef:
                  name: aws-secrets
                  key: export-s3-access-key
            - name: s3.access-secret
              valueFrom:
                secretKeyRef:
                  name: aws-secrets
                  key: export-s3-access-key-secret
            - name: token.secret
              valueFrom:
                secretKeyRef:
                  name: aws-secrets
                  key: ${tokenSecretName}
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
            - name: aws-secrets
              mountPath: "/mnt/secrets-store/aws-secrets"
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
