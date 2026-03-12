# Namespace

In the Linux kernel, **namespace is a fundamental attribute of every process, not a separate virtualization layer**. Each process is bound to a set of namespaces via the `nsproxy` structure in its process descriptor, defining its isolated view of system resources.

```c
/**
 * Linux 6.8 Kernel Source References
 * https://elixir.bootlin.com/linux/v6.8/source
 */

// Process descriptor (include/linux/sched.h)
struct task_struct {
	/* Namespace proxy: links process to all its namespaces */
	struct nsproxy			*nsproxy;
};

// Namespace collection structure (include/linux/nsproxy.h)
struct nsproxy {
	refcount_t count;
	struct uts_namespace *uts_ns;
	struct ipc_namespace *ipc_ns;
	struct mnt_namespace *mnt_ns;
	struct pid_namespace *pid_ns_for_children;
	struct net 	     *net_ns;
	struct time_namespace *time_ns;
	struct time_namespace *time_ns_for_children;
	struct cgroup_namespace *cgroup_ns;
};
```

## Kubernetes Containers

A Kubernetes Pod is the minimal cluster unit, consisting of processes sharing a dedicated namespace set. **Container processes are identical to physical host processes at the kernel level** — the only difference is their attached namespace.

Discrepancies in `ps` output stem from **the bash shell’s namespace context**, not the process itself. The same process shows different PIDs when viewed from the container shell (isolated NS) and host shell (global NS).

```shell
# Inspect kube-flannel Pod (K8s cluster)
$ kubectl get pods kube-flannel-ds-4g9kr -n kube-flannel -o wide
NAME                    READY   STATUS    RESTARTS   AGE    IP               NODE              NOMINATED NODE   READINESS GATES
kube-flannel-ds-4g9kr   1/1     Running   0          145m   192.168.66.211   k8sprodmaster01   <none>           <none>

# Exec into container: PID shows as 1 (container PID NS)
$ kubectl exec -n kube-flannel -it kube-flannel-ds-4g9kr -- bash
k8sprodmaster01:/# ps
PID   USER     TIME  COMMAND
    1 root      1h13 /opt/bin/flanneld --ip-masq --kube-subnet-mgr

# Check host machine: same process has global PID 30497
ps -ef | grep flannel
root       30497   30277  0 Mar02 ?        01:13:26 /opt/bin/flanneld --ip-masq --kube-subnet-mgr

# Verify process namespace binding (host)
$ sudo ls -l /proc/30497/ns
total 0
lrwxrwxrwx 1 root root 0 Mar  3 00:05 cgroup -> 'cgroup:[4026532310]'
lrwxrwxrwx 1 root root 0 Mar  3 00:05 ipc -> 'ipc:[4026532306]'
lrwxrwxrwx 1 root root 0 Mar  2 23:04 mnt -> 'mnt:[4026532308]'
lrwxrwxrwx 1 root root 0 Mar  3 00:05 net -> 'net:[4026531840]'
lrwxrwxrwx 1 root root 0 Mar  3 00:05 pid -> 'pid:[4026532309]'
lrwxrwxrwx 1 root root 0 Mar  3 00:45 pid_for_children -> 'pid:[4026532309]'
lrwxrwxrwx 1 root root 0 Mar  3 00:45 time -> 'time:[4026531834]'
lrwxrwxrwx 1 root root 0 Mar  3 00:45 time_for_children -> 'time:[4026531834]'
lrwxrwxrwx 1 root root 0 Mar  3 00:45 user -> 'user:[4026531837]'
lrwxrwxrwx 1 root root 0 Mar  3 00:05 uts -> 'uts:[4026531838]'

# List namespaces tied to the flanneld process
$ sudo lsns
        NS TYPE   NPROCS   PID USER   COMMAND
4026532310 cgroup      1 30497 root             /opt/bin/flanneld --ip-masq --kube-subnet-mgr
4026532306 ipc         2 30325 65535            /pause
4026532308 mnt         1 30497 root             /opt/bin/flanneld --ip-masq --kube-subnet-mgr
4026531840 net       220     1 root             /usr/lib/systemd/systemd --system --deserialize=61
4026532309 pid         1 30497 root             /opt/bin/flanneld --ip-masq --kube-subnet-mgr
4026531834 time      224     1 root             /usr/lib/systemd/systemd --system --deserialize=61
4026531837 user      223     1 root             /usr/lib/systemd/systemd --system --deserialize=61
4026531838 uts       215     1 root             /usr/lib/systemd/systemd --system --deserialize=61

# Dedicated cgroup, ipc, mnt, pid namespaces confirmed
```

## Kernel Logic

The `getpid()` system call reveals the kernel’s namespace-aware PID handling. It does not return a fixed global PID, but a numeric ID mapped to **the caller’s active PID namespace**.

```c
// getpid() syscall entry (kernel/sys.c)
SYSCALL_DEFINE0(getpid)
{
	return task_tgid_vnr(current);
}

// Fetch TGID relative to current process NS (include/linux/pid.h)
static inline pid_t task_tgid_vnr(struct task_struct *tsk)
{
	return __task_pid_nr_ns(tsk, PIDTYPE_TGID, NULL);
}

// Core PID lookup with namespace parameter (kernel/pid.c)
pid_t __task_pid_nr_ns(struct task_struct *task, enum pid_type type,
			struct pid_namespace *ns)
{
	pid_t nr = 0;

	rcu_read_lock();
	if (!ns)
		ns = task_active_pid_ns(current);
	nr = pid_nr_ns(rcu_dereference(*task_pid_ptr(task, type)), ns);
	rcu_read_unlock();

	return nr;
}

// Map PID to target namespace number (kernel/pid.c)
pid_t pid_nr_ns(struct pid *pid, struct pid_namespace *ns)
{
	struct upid *upid;
	pid_t nr = 0;

	if (pid && ns->level <= pid->level) {
		upid = &pid->numbers[ns->level];
		if (upid->ns == ns)
			nr = upid->nr;
	}
	return nr;
}                   
```