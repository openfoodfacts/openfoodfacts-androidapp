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

desc "Run CI checks"

TESTS = ["test", "integration", "spec", "rubocop", "yardoc", "build", "linkinator"]

flag :only
TESTS.each do |name|
  flag "include_#{name}".to_sym, "--[no-]include-#{name}"
end

include :exec, result_callback: :handle_result
include :terminal

def handle_result result
  if result.success?
    puts "** #{result.name} passed\n\n", :green, :bold
  else
    puts "** CI terminated: #{result.name} failed!", :red, :bold
    exit 1
  end
end

def run
  ::Dir.chdir context_directory
  TESTS.each do |name|
    setting = get "include_#{name}".to_sym
    setting = !only if setting.nil?
    exec ["toys", name], name: name.capitalize if setting
  end
end
