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

include "kokoro-tools"
include :exec, e: true
include :fileutils
include :gems

flag :rubygems_token, "--rubygems-token=TOKEN"
flag :dry_run, default: ["true", "gem"].include?(::ENV["RELEASE_DRY_RUN"].to_s)

def run
  gem "gems", "~> 1.2"
  require "gems"
  ::Dir.chdir package_directory
  load_env
  configure_gems
  gem_path = build_gem
  if dry_run
    puts "DRY RUN: Skipping Rubygems push of #{gem_path}"
  else
    push_gem gem_path
  end
end

def configure_gems
  token = rubygems_token || ::ENV["RUBYGEMS_API_TOKEN"]
  ::Gems.configure { |config| config.key = token } if token
end

def build_gem
  gem_path = "pkg/#{package_name}-#{package_version}.gem"
  rm_rf gem_path
  exec ["toys", "build"]
  gem_path
end

def push_gem gem_path
  response = ::Gems.push ::File.new gem_path
  puts response
  raise "Gem push didn't report success" unless response.include? "Successfully registered gem:"
end
