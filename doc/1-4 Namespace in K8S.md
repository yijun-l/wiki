# Namespace in K8s

Now we understand Linux namespaces are process attributes. Each namespace type restricts a process’s visibility and access to a specific category of system resources, creating isolated "virtual environments" for processes.

Then we will see how namespaces are used in Kubernetes by tracing the full lifecycle of a Pod creation.

## Tracing a Pod Creation

A Pod is created and managed by `containerd`. We use `strace` to monitor the `containerd` process, tracking the three most critical system calls:

- `clone()`: Creates a new process with configurable shared or isolated resources
- `unshare()`: Detaches a process from its current namespaces and creates new isolated namespaces
- `execve()`: Executes a new program (e.g., container runtime shim, container process)

```shell
$ sudo strace -f -e trace=clone,unshare,execve -p `pidof containerd` -o strace_log
strace: Process 1696928 attached with 10 threads
```
When we delete a Kubernetes Pod, K8s automatically recreates a new Pod to maintain the desired state. This triggers the complete `containerd` Pod creation workflow, which we capture in the `strace_log`.

The strace logs reveal the standard layered container creation pipeline: containerd > shim > runc > container processes

The `unshare()` system call is the core operation that enables namespace isolation.

```shell
## 1. Containerd spawns K8s Pod shim process
1886354 execve("/usr/bin/containerd-shim-runc-v2", ["/usr/bin/containerd-shim-runc-v2", "-namespace", "k8s.io", "-id", "1d37f6fcc809a350d6229b6099cb0151"..., "-address", "/run/containerd/containerd.sock"], 0xc0001aeb80 /* 15 vars */ <unfinished ...>

## 2. Shim invokes runc
  1886375 execve("/usr/sbin/runc", ["runc", "--root", "/run/containerd/runc/k8s.io", "--log", "/run/containerd/io.containerd.ru"..., "--log-format", "json", "--systemd-cgroup", "create", "--bundle", "/run/containerd/io.containerd.ru"..., "--pid-file", "/run/containerd/io.containerd.ru"..., "1d37f6fcc809a350d6229b6099cb0151"...], 0xc000188a80 /* 14 vars */ <unfinished ...>

## 3. Runc creates NEW namespaces for the Pod pause container (infrastructure container)
    1886397 unshare(CLONE_NEWNS|CLONE_NEWUTS|CLONE_NEWIPC|CLONE_NEWPID) = 0
    1886397 clone(child_stack=0x7ffcd5331f70, flags=CLONE_PARENT|SIGCHLD) = 1886399
    
      1886399 execve("/pause", ["/pause"], 0xc000131578 /* 2 vars */ <unfinished ...>


  1886436 execve("/usr/sbin/runc", ["runc", "--root", "/run/containerd/runc/k8s.io", "--log", "/run/containerd/io.containerd.ru"..., "--log-format", "json", "--systemd-cgroup", "create", "--bundle", "/run/containerd/io.containerd.ru"..., "--pid-file", "/run/containerd/io.containerd.ru"..., "b36b7130eb29b06765ae9349b969dd60"...], 0xc0000ce300 /* 14 vars */ <unfinished ...>
    
    1886447 unshare(CLONE_NEWNS|CLONE_NEWCGROUP|CLONE_NEWPID) = 0
    1886447 clone(child_stack=0x7fff26485770, flags=CLONE_PARENT|SIGCHLD) = 1886448
  
      1886448 execve("/opt/bitnami/openldap/sbin/slapd", ["/opt/bitnami/openldap/sbin/slapd", "-h", "ldap://:1389/", "-F", "/opt/bitnami/openldap/etc/slapd."..., "-d", "32768"], 0x5d26ca044070 /* 117 vars */) = 0
```

We can inspect the `/proc/[PID]/ns` directory to verify namespace isolation for the Pod’s `pause` and `openldap` processes:

```shell
## List Pod processes (1886399 = pause, 1886448 = OpenLDAP)
$ ps -ef | grep 1886354
root     1886354       1  0 18:56 ?        00:00:02 /usr/bin/containerd-shim-runc-v2 -namespace k8s.io -id 1d37f6fcc809a350d6229b6099cb01511afae3de0e9f7fa9baaa812b2f5e6cbb -address /run/containerd/containerd.sock
65535    1886399 1886354  0 18:56 ?        00:00:00 /pause
1001     1886448 1886354  0 18:56 ?        00:00:00 /opt/bitnami/openldap/sbin/slapd -h ldap://:1389/ -F /opt/bitnami/openldap/etc/slapd.d -d 32768

## Check pause container namespaces (Pod infrastructure base)
$ ls -l /proc/1886399/ns
total 0
lrwxrwxrwx 1 65535 65535 0 Mar 14 19:58 cgroup -> 'cgroup:[4026531835]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 18:56 ipc -> 'ipc:[4026532521]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 18:56 mnt -> 'mnt:[4026532519]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 18:56 net -> 'net:[4026532461]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 19:58 pid -> 'pid:[4026532522]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 19:58 pid_for_children -> 'pid:[4026532522]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 19:58 time -> 'time:[4026531834]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 19:58 time_for_children -> 'time:[4026531834]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 19:58 user -> 'user:[4026531837]'
lrwxrwxrwx 1 65535 65535 0 Mar 14 18:56 uts -> 'uts:[4026532520]'

## Check OpenLDAP container namespaces, which has dedicated mount, cgroup, PID namespaces 
$ ls -l /proc/1886448/ns
total 0
lrwxrwxrwx 1 root root 0 Mar 14 19:05 cgroup -> 'cgroup:[4026532720]'
lrwxrwxrwx 1 root root 0 Mar 14 19:05 ipc -> 'ipc:[4026532521]'
lrwxrwxrwx 1 root root 0 Mar 14 18:56 mnt -> 'mnt:[4026532718]'
lrwxrwxrwx 1 root root 0 Mar 14 19:05 net -> 'net:[4026532461]'
lrwxrwxrwx 1 root root 0 Mar 14 19:05 pid -> 'pid:[4026532719]'
lrwxrwxrwx 1 root root 0 Mar 14 19:59 pid_for_children -> 'pid:[4026532719]'
lrwxrwxrwx 1 root root 0 Mar 14 19:59 time -> 'time:[4026531834]'
lrwxrwxrwx 1 root root 0 Mar 14 19:59 time_for_children -> 'time:[4026531834]'
lrwxrwxrwx 1 root root 0 Mar 14 19:59 user -> 'user:[4026531837]'
lrwxrwxrwx 1 root root 0 Mar 14 19:05 uts -> 'uts:[4026532520]'
```

The OpenLDAP container **shares core namespaces** (IPC, NET, UTS) with the `pause` container.

Then it calls `1886447 unshare(CLONE_NEWNS|CLONE_NEWCGROUP|CLONE_NEWPID) = 0` to create dedicated mount, cgroup, and PID namespaces for isolation.

This matches the namespace files we inspected: **shared network/IPC/UTS**, **private mount/cgroup/PID**.

## Understand Namespaces with the `unshare` CLI Utility

The `unshare` user-space tool directly invokes the kernel’s `unshare()` system call. 

It allows us to manually test each namespace type and verify isolation behavior, just like container runtimes do.

### 1. UTS Namespace (Isolate Hostname)

`-u`: Create a new UTS namespace — changes to the hostname only affect the isolated shell, not the host system.

```shell
$ sudo unshare -u bash

$ hostname test
$ hostname
test

$ exit
$ hostname
worker02
```
### 2. Network Namespace (Isolate Network Interfaces/IP/Routes)

`-n`: Create a new network namespace — the isolated shell has only a loopback interface (no host network access).

```shell
$ sudo unshare -n bash
$ ip addr
1: lo: <LOOPBACK> mtu 65536 qdisc noop state DOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
```
### 3. PID Namespace (Isolate Process IDs)

`-p -f --mount-proc`: Create a new PID namespace, fork a child process, and mount a new `/proc` filesystem — the first process in the namespace has PID 1 (like a container init process).

```shell
$ sudo unshare -p -f --mount-proc bash
$ ps 
    PID TTY          TIME CMD
      1 pts/1    00:00:00 bash
      4 pts/1    00:00:00 ps
```

### 4. User Namespace (Isolate UID/GID Permissions)

`-U --map-root-user`: Create a new user namespace — maps the current user to root (UID 0) inside the namespace without host root privileges.

```shell
$ sudo unshare -U --map-root-user bash
$ id
uid=0(root) gid=0(root) groups=0(root)
```

### 5. Mount Namespace (Isolate Filesystem Mounts)

`-m`: Create a new mount namespace — mounts created inside the namespace are invisible to the host.

```shell
$ sudo unshare -m bash
$ mount -t tmpfs tmpfs /mnt
$ touch /mnt/test

$ exit
$ ls /mnt
```
### 6. Cgroup Namespace (Isolate Cgroup Visibility)

`-C`: Create a new cgroup namespace — processes see an isolated cgroup hierarchy (root / instead of the host’s cgroup path).

```shell
$ cat /proc/self/cgroup
0::/user.slice/user-1000.slice/session-70.scope
$  sudo unshare -C bash
$  cat /proc/self/cgroup
0::/
```

### 7. Time Namespace (Isolate System Time/Boot Time)
`-T --boottime`: Create a new time namespace — customizes the perceived boot time (isolated time for the process).

```shell
$ sudo unshare -T --boottime 86400 bash
$ uptime
 21:54:39 up 90 days, 31 min,  3 users,  load average: 0.40, 0.22, 0.13
$ exit
$ uptime
 21:54:44 up 89 days, 31 min,  3 users,  load average: 0.37, 0.22, 0.13
```

### 8. IPC Namespace (Isolate Inter-Process Communication)

`-i`: Create a new IPC namespace — shared memory/semaphores/message queues are isolated from the host.

```shell
$ sudo unshare -i bash
$ ipcmk -M 1024
Shared memory id: 0
$ ipcs -m

------ Shared Memory Segments --------
key        shmid      owner      perms      bytes      nattch     status
0xfbf1a9d9 0          root       644        1024       0

$ exit
$ ipcs -m

------ Shared Memory Segments --------
key        shmid      owner      perms      bytes      nattch     status
```