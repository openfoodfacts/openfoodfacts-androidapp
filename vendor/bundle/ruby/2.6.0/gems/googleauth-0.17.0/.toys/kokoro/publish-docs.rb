# Copyright 2021 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require "json"

include "kokoro-tools"
include :exec, e: true
include :fileutils

flag :credentials, "--credentials=PATH"
flag :bucket, "--bucket=NAME"
flag :dry_run, default: ["true", "docs"].include?(::ENV["RELEASE_DRY_RUN"].to_s)

def run
  ::Dir.chdir package_directory
  load_env
  build_docs
  write_metadata
  if dry_run
    puts "DRY RUN: Skipping doc uploading for #{package_name}"
  else
    upload_docs
  end
end

def build_docs
  rm_rf "doc"
  exec ["toys", "yardoc"]
end

def write_metadata
  allowed_fields = [
    "name", "version", "language", "distribution-name",
    "product-page", "github-repository", "issue-tracker"
  ]
  metadata = ::JSON.parse ::File.read ".repo-metadata.json"
  metadata.transform_keys! { |k| k.tr "_", "-" }
  metadata.keep_if { |k, _v| allowed_fields.include? k }
  metadata["version"] = package_version
  metadata["name"] = metadata["distribution-name"]
  args = metadata.transform_keys { |k| "--#{k}" }.to_a.flatten
  cmd = ["python3", "-m", "docuploader", "create-metadata"] + args
  exec cmd, chdir: "doc"
end

def upload_docs
  creds = credentials || "#{::ENV['KOKORO_KEYSTORE_DIR']}/73713_docuploader_service_account"
  buck = bucket || ::ENV["STAGING_BUCKET"] || "docs-staging"
  cmd = [
    "python3", "-m", "docuploader", "upload", ".",
    "--credentials=#{creds}",
    "--staging-bucket=#{buck}",
    "--metadata-file=./docs.metadata"
  ]
  exec cmd, chdir: "doc"
end
