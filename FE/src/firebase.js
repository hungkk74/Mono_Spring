import { initializeApp } from 'firebase/app';
import { getStorage, ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';

const firebaseConfig = {
  apiKey: "AIzaSyDZr-k5hCgQPCwN3-n_WIx5nCKkdFJenq0",
  authDomain: "mono-webshop.firebaseapp.com",
  projectId: "mono-webshop",
  storageBucket: "mono-webshop.firebasestorage.app",
  messagingSenderId: "858781179160",
  appId: "1:858781179160:web:affa25d0f8950a3bdd526d",
  measurementId: "G-HF38RPQ7HV",
};

const app = initializeApp(firebaseConfig);
const storage = getStorage(app);

/**
 * Upload file lên Firebase Storage.
 * @param {File} file - File ảnh từ input
 * @param {function} onProgress - Callback nhận % progress (0-100)
 * @returns {Promise<string>} Download URL
 */
export async function uploadImage(file, onProgress) {
  const ext = file.name.split('.').pop();
  const fileName = `products/${Date.now()}_${Math.random().toString(36).slice(2)}.${ext}`;
  const storageRef = ref(storage, fileName);

  return new Promise((resolve, reject) => {
    const uploadTask = uploadBytesResumable(storageRef, file);

    uploadTask.on(
      'state_changed',
      (snapshot) => {
        const progress = Math.round((snapshot.bytesTransferred / snapshot.totalBytes) * 100);
        onProgress?.(progress);
      },
      (error) => reject(error),
      async () => {
        const url = await getDownloadURL(uploadTask.snapshot.ref);
        resolve(url);
      }
    );
  });
}

export { storage };

/**
 * Upload file (image hoặc video) lên Firebase Storage.
 * @param {File} file - File từ input
 * @param {string} folder - Folder trên Storage (vd: 'banners', 'products')
 * @param {function} onProgress - Callback nhận % progress (0-100)
 * @returns {Promise<string>} Download URL
 */
export async function uploadMedia(file, folder = 'banners', onProgress) {
  const ext = file.name.split('.').pop();
  const fileName = `${folder}/${Date.now()}_${Math.random().toString(36).slice(2)}.${ext}`;
  const storageRef = ref(storage, fileName);

  return new Promise((resolve, reject) => {
    const uploadTask = uploadBytesResumable(storageRef, file);

    uploadTask.on(
      'state_changed',
      (snapshot) => {
        const progress = Math.round((snapshot.bytesTransferred / snapshot.totalBytes) * 100);
        onProgress?.(progress);
      },
      (error) => reject(error),
      async () => {
        const url = await getDownloadURL(uploadTask.snapshot.ref);
        resolve(url);
      }
    );
  });
}
