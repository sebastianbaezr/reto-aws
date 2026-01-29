const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const DIST_DIR = path.join(__dirname, 'dist');
const SRC_DIR = path.join(__dirname, 'src');

console.log('🔨 Building Node.js Lambda deployment package...\n');

// Step 1: Clean and create dist directory
console.log('1️⃣  Creating dist directory...');
if (fs.existsSync(DIST_DIR)) {
  fs.rmSync(DIST_DIR, { recursive: true, force: true });
}
fs.mkdirSync(DIST_DIR, { recursive: true });

// Step 2: Copy source files
console.log('2️⃣  Copying source files...');
function copyDir(src, dest) {
  if (!fs.existsSync(dest)) {
    fs.mkdirSync(dest, { recursive: true });
  }

  const entries = fs.readdirSync(src);
  entries.forEach(entry => {
    const srcPath = path.join(src, entry);
    const destPath = path.join(dest, entry);
    const stat = fs.statSync(srcPath);

    if (stat.isDirectory()) {
      copyDir(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  });
}

copyDir(SRC_DIR, path.join(DIST_DIR, 'src'));

// Step 3: Copy package.json (remove dev dependencies section)
console.log('3️⃣  Preparing package.json...');
const packageJson = JSON.parse(fs.readFileSync(path.join(__dirname, 'package.json'), 'utf8'));
delete packageJson.devDependencies;
delete packageJson.scripts;
fs.writeFileSync(
  path.join(DIST_DIR, 'package.json'),
  JSON.stringify(packageJson, null, 2)
);

// Step 4: Install production dependencies in dist
console.log('4️⃣  Installing production dependencies...');
try {
  execSync('npm install --omit=dev', { cwd: DIST_DIR, stdio: 'inherit' });
} catch (error) {
  console.error('Error installing dependencies:', error.message);
  process.exit(1);
}

// Step 5: Create ZIP file
console.log('\n5️⃣  Creating deployment ZIP...');
const zipPath = path.join(__dirname, 'nodejs-lambda.zip');
try {
  const archiver = require('archiver');
  const output = fs.createWriteStream(zipPath);
  const archive = archiver('zip', { zlib: { level: 9 } });

  output.on('close', () => {
    console.log(`   ✓ Created ZIP archive: nodejs-lambda.zip (${archive.pointer()} bytes)`);
  });

  archive.on('error', (err) => {
    throw err;
  });

  archive.pipe(output);
  archive.directory(DIST_DIR + '/', false);
  archive.finalize();
} catch (error) {
  console.error('Error creating ZIP with archiver:', error.message);
  console.error('Note: ZIP creation failed, but dist/ directory is ready for deployment');
  console.log('   You can manually create the ZIP or upload the dist/ directory');
}

console.log('\n✅ Build complete!\n');
console.log('📦 Deployment package location:');
console.log(`   - Directory: ./dist/`);
console.log(`   - ZIP file: ./nodejs-lambda.zip (if created)\n`);
console.log('📋 Next steps:');
console.log('   1. Upload dist/ directory OR nodejs-lambda.zip to S3:');
console.log(`      aws s3 cp dist s3://your-bucket/nodejs-lambda/ --recursive`);
console.log(`      OR`);
console.log(`      aws s3 cp nodejs-lambda.zip s3://your-bucket/`);
console.log('\n   2. Use the S3 path in CloudFormation template:');
console.log(`      ParameterKey=NodeLambdaZipPath,ParameterValue=s3://your-bucket/nodejs-lambda.zip`);
